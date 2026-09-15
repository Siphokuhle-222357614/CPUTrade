package za.ac.cput.cputrade.product;

import za.ac.cput.cputrade.chat.ChatMessageRepository;
import za.ac.cput.cputrade.chat.Conversation;
import za.ac.cput.cputrade.chat.ConversationRepository;
import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.notification.NotificationService;
import za.ac.cput.cputrade.notification.NotificationType;
import za.ac.cput.cputrade.product.dto.AddImagesRequest;
import za.ac.cput.cputrade.product.dto.BuyerSummary;
import za.ac.cput.cputrade.product.dto.MarkSoldRequest;
import za.ac.cput.cputrade.product.dto.ProductCreateRequest;
import za.ac.cput.cputrade.product.dto.ProductResponse;
import za.ac.cput.cputrade.product.dto.ProductUpdateRequest;
import za.ac.cput.cputrade.rating.RatingService;
import za.ac.cput.cputrade.rating.dto.RatingSummary;
import za.ac.cput.cputrade.storage.ImageStorageService;
import za.ac.cput.cputrade.user.Role;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    /** Hard cap on photos per listing — generous for a resale marketplace, stingy enough to bound storage/abuse. */
    private static final int MAX_IMAGES_PER_PRODUCT = 6;

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RatingService ratingService;
    private final ImageStorageService imageStorageService;
    private final WishlistItemRepository wishlistItemRepository;
    private final NotificationService notificationService;
    private final SavedSearchService savedSearchService;

    public ProductService(ProductRepository productRepository, UserRepository userRepository,
                           ConversationRepository conversationRepository, ChatMessageRepository chatMessageRepository,
                           RatingService ratingService, ImageStorageService imageStorageService,
                           WishlistItemRepository wishlistItemRepository, NotificationService notificationService,
                           SavedSearchService savedSearchService) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.ratingService = ratingService;
        this.imageStorageService = imageStorageService;
        this.wishlistItemRepository = wishlistItemRepository;
        this.notificationService = notificationService;
        this.savedSearchService = savedSearchService;
    }

    /** US3.1 category, US3.2 keyword, US3.3 price range — any combination, all optional. */
    public List<ProductResponse> search(Category category, String keyword, BigDecimal minPrice, BigDecimal maxPrice) {
        List<Specification<Product>> filters = new ArrayList<>();
        filters.add(ProductSpecifications.isActive());
        // Sold-out listings have nothing left to buy — keep them off the browse/search
        // feed, but their direct link still works (see getActiveById) so a buyer who
        // already has it open (e.g. to leave a rating) isn't locked out.
        filters.add(ProductSpecifications.notSold());
        if (category != null) {
            filters.add(ProductSpecifications.hasCategory(category));
        }
        if (keyword != null && !keyword.isBlank()) {
            filters.add(ProductSpecifications.keywordMatches(keyword));
        }
        if (minPrice != null) {
            filters.add(ProductSpecifications.priceGte(minPrice));
        }
        if (maxPrice != null) {
            filters.add(ProductSpecifications.priceLte(maxPrice));
        }

        Specification<Product> combined = filters.stream().reduce(Specification::and).orElse(null);
        List<Product> products = productRepository.findAll(combined, Sort.by(Sort.Direction.DESC, "createdAt"));
        return products.stream().map(this::toResponse).toList();
    }

    /**
     * The 10 most recently completed sales, across every seller — public
     * social proof that the marketplace is actually active, not a private
     * business metric (nothing here isn't already visible on each listing's
     * own now-sold detail page).
     */
    public List<ProductResponse> recentlySold() {
        return productRepository.findTop10BySoldTrueOrderBySoldAtDesc().stream().map(this::toResponse).toList();
    }

    /** A seller's business dashboard — every listing they own, active/sold/inactive, newest first. */
    public List<ProductResponse> listMine(Authentication auth) {
        User seller = currentUser(auth);
        return productRepository.findBySellerIdOrderByCreatedAtDesc(seller.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse getActiveById(Long id) {
        Product product = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> ApiException.notFound("Listing not found"));
        // US2.4: count each detail-page open, not each appearance in search results.
        product.setViewCount(product.getViewCount() + 1);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse create(ProductCreateRequest request, Authentication auth) {
        User seller = currentUser(auth);

        // US1.5: a VENDOR can register and log in immediately, but cannot sell until approved.
        if (seller.getRole() == Role.VENDOR && !seller.isVendorApproved()) {
            throw ApiException.forbidden("Your vendor account is pending admin approval");
        }

        List<String> imageUrls = storeAll(request.getImages());

        Product product = Product.builder()
                .seller(seller)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .condition(request.getCondition())
                .quantity(request.getQuantityOrDefault())
                .imageUrls(imageUrls)
                .active(true)
                .build();

        Product saved = productRepository.save(product);
        try {
            savedSearchService.notifyMatchingSavedSearches(saved);
        } catch (Exception e) {
            // A saved-search alert failing must never roll back the listing it was checking.
            log.warn("Could not check saved searches for new product {}", saved.getId(), e);
        }
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request, Authentication auth) {
        Product product = findByIdOrThrow(id);
        requireOwnerOrAdmin(product, auth);

        BigDecimal previousPrice = product.getPrice();
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setCondition(request.getCondition());
        product.setQuantity(request.getQuantity());

        Product saved = productRepository.save(product);
        if (previousPrice != null && request.getPrice().compareTo(previousPrice) < 0) {
            notifyWishlisters(saved, NotificationType.PRICE_DROP,
                    "Price drop! \"" + saved.getTitle() + "\" is now R" + saved.getPrice() + " (was R" + previousPrice + ")");
        }
        return toResponse(saved);
    }

    /** Appends new photos (owner/admin), up to the per-listing cap. */
    @Transactional
    public ProductResponse addImages(Long id, AddImagesRequest request, Authentication auth) {
        Product product = findByIdOrThrow(id);
        requireOwnerOrAdmin(product, auth);

        int existing = product.getImageUrls().size();
        int incoming = request.getImages().size();
        if (existing + incoming > MAX_IMAGES_PER_PRODUCT) {
            throw ApiException.badRequest(
                    "This listing already has " + existing + " photo(s) — up to "
                            + MAX_IMAGES_PER_PRODUCT + " are allowed in total");
        }

        product.getImageUrls().addAll(storeAll(request.getImages()));
        return toResponse(productRepository.save(product));
    }

    /** Removes one photo by URL (owner/admin) — the rest keep their relative order. */
    @Transactional
    public ProductResponse removeImage(Long id, String imageUrl, Authentication auth) {
        Product product = findByIdOrThrow(id);
        requireOwnerOrAdmin(product, auth);

        boolean removed = product.getImageUrls().remove(imageUrl);
        if (!removed) {
            throw ApiException.notFound("That photo isn't on this listing");
        }
        imageStorageService.delete(imageUrl);
        return toResponse(productRepository.save(product));
    }

    /** Seller-only: who has messaged them about this listing, to pick from when marking it sold. */
    public List<BuyerSummary> interestedBuyers(Long id, Authentication auth) {
        Product product = findByIdOrThrow(id);
        requireOwnerOrAdmin(product, auth);

        // A buyer can message once but send many messages — de-dupe by user, keep first-seen order.
        Map<Long, BuyerSummary> byBuyerId = new LinkedHashMap<>();
        for (Conversation conversation : conversationRepository.findByProductIdOrderByCreatedAtDesc(id)) {
            User buyer = conversation.getBuyer();
            byBuyerId.putIfAbsent(buyer.getId(), BuyerSummary.builder().id(buyer.getId()).username(buyer.getUsername()).build());
        }
        return List.copyOf(byBuyerId.values());
    }

    /**
     * Records one unit sold (owner/admin) — decrements {@link Product#quantity}
     * by one rather than always closing out the whole listing, so a seller
     * with several identical units (e.g. 4 pens) can sell them one at a time
     * and the listing only disappears from marketplace search once the last
     * one goes. Only the sale that empties the stock sets {@code soldTo}/
     * {@code soldAt}/{@code sold=true} — there's no per-unit buyer record for
     * whoever bought the ones before it, since nothing here tracks individual
     * orders (see the {@code quantity} javadoc on {@link Product}).
     */
    @Transactional
    public ProductResponse markSold(Long id, MarkSoldRequest request, Authentication auth) {
        Product product = findByIdOrThrow(id);
        requireOwnerOrAdmin(product, auth);

        if (product.isSold()) {
            throw ApiException.badRequest("This listing is already marked sold");
        }

        User soldTo = null;
        if (request.getSoldToUserId() != null) {
            boolean messagedAboutThisListing = conversationRepository
                    .findByProductIdAndBuyerId(id, request.getSoldToUserId())
                    .isPresent();
            if (!messagedAboutThisListing) {
                throw ApiException.badRequest("That user hasn't messaged you about this listing");
            }
            soldTo = userRepository.findById(request.getSoldToUserId())
                    .orElseThrow(() -> ApiException.notFound("User not found"));
        }

        boolean soldOut = product.getQuantity() <= 1;
        product.setQuantity(Math.max(product.getQuantity() - 1, 0));
        if (soldOut) {
            product.setSold(true);
            product.setSoldAt(LocalDateTime.now());
            product.setSoldTo(soldTo);
        }
        Product saved = productRepository.save(product);
        if (soldOut) {
            notifyWishlisters(saved, NotificationType.WISHLIST_ITEM_SOLD,
                    "\"" + saved.getTitle() + "\" was just sold — better luck with the next one!");
        }
        return toResponse(saved);
    }

    /**
     * Undo a mistaken "mark as sold" (owner/admin) — puts the listing back in
     * marketplace search. Also restores one unit of stock if the listing had
     * sold all the way out (quantity 0) — there's no decrement history to
     * restore from, so this assumes the mistake being undone is the one that
     * just closed it out.
     */
    @Transactional
    public ProductResponse markAvailable(Long id, Authentication auth) {
        Product product = findByIdOrThrow(id);
        requireOwnerOrAdmin(product, auth);

        product.setSold(false);
        product.setSoldAt(null);
        product.setSoldTo(null);
        if (product.getQuantity() <= 0) {
            product.setQuantity(1);
        }
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id, Authentication auth) {
        Product product = findByIdOrThrow(id);
        requireOwnerOrAdmin(product, auth);

        // All of this must go before the product row itself — every one of these
        // tables has a NOT NULL FK onto products.id. Deleting a listing a buyer had
        // already messaged about used to 500 (a real bug, not hypothetical: found by
        // deleting a listing with a conversation on it) because only the wishlist
        // side of this was ever cleaned up. Messages before conversations, since
        // chat_messages.conversation_id is itself a NOT NULL FK onto conversations.
        List<Long> conversationIds = conversationRepository.findByProductIdOrderByCreatedAtDesc(id).stream()
                .map(Conversation::getId)
                .toList();
        if (!conversationIds.isEmpty()) {
            // An empty IN (...) clause is invalid JPQL — Hibernate would throw before
            // ever reaching the database, so this only runs when there's something to delete.
            chatMessageRepository.deleteByConversationIdIn(conversationIds);
        }
        conversationRepository.deleteByProductId(id);
        wishlistItemRepository.deleteByProductId(id);

        productRepository.delete(product);
        for (String imageUrl : product.getImageUrls()) {
            imageStorageService.delete(imageUrl);
        }
    }

    /** Public so AdminService can reuse the same rating-aware mapping. */
    public ProductResponse toResponse(Product product) {
        RatingSummary sellerRating = ratingService.summarize(product.getSeller().getId());
        long sellerCompletedSales = productRepository.countBySellerIdAndSoldTrue(product.getSeller().getId());
        long watcherCount = wishlistItemRepository.countByProductId(product.getId());
        return ProductResponse.from(product, sellerRating, sellerCompletedSales, watcherCount);
    }

    /** Notifies everyone watching this listing, except the seller themselves (can't wishlist your own listing anyway). */
    private void notifyWishlisters(Product product, NotificationType type, String message) {
        for (WishlistItem item : wishlistItemRepository.findByProductId(product.getId())) {
            try {
                notificationService.create(item.getUser(), type, message, "/products/" + product.getId());
            } catch (Exception e) {
                // A notification failure (for one watcher, or all of them) must never roll
                // back the price-drop/mark-sold update this runs inside of.
                log.warn("Could not notify wishlist watcher {} about product {}", item.getUser().getId(), product.getId(), e);
            }
        }
    }

    private List<String> storeAll(List<String> base64Images) {
        if (base64Images == null || base64Images.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> urls = new ArrayList<>(base64Images.size());
        for (String base64Image : base64Images) {
            urls.add(imageStorageService.store(base64Image)); // validates, then writes to disk
        }
        return urls;
    }

    private Product findByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Listing not found"));
    }

    private void requireOwnerOrAdmin(Product product, Authentication auth) {
        User user = currentUser(auth);
        boolean isOwner = product.getSeller().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw ApiException.forbidden("You may only modify your own listings");
        }
    }

    private User currentUser(Authentication auth) {
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.unauthorized("Unknown user"));
    }
}
