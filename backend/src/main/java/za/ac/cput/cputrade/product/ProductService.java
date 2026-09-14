package za.ac.cput.cputrade.product;

import za.ac.cput.cputrade.common.ImageValidator;
import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.product.dto.ProductCreateRequest;
import za.ac.cput.cputrade.product.dto.ProductResponse;
import za.ac.cput.cputrade.product.dto.ProductUpdateRequest;
import za.ac.cput.cputrade.rating.RatingService;
import za.ac.cput.cputrade.rating.dto.RatingSummary;
import za.ac.cput.cputrade.user.Role;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final RatingService ratingService;

    public ProductService(ProductRepository productRepository, UserRepository userRepository, RatingService ratingService) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.ratingService = ratingService;
    }

    /** US3.1 category, US3.2 keyword, US3.3 price range — any combination, all optional. */
    public List<ProductResponse> search(Category category, String keyword, BigDecimal minPrice, BigDecimal maxPrice) {
        List<Specification<Product>> filters = new ArrayList<>();
        filters.add(ProductSpecifications.isActive());
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

        String decodedImage = null;
        if (request.getImageBase64() != null && !request.getImageBase64().isBlank()) {
            ImageValidator.validate(request.getImageBase64()); // throws on invalid/oversized/wrong-type
            decodedImage = request.getImageBase64();
        }

        Product product = Product.builder()
                .seller(seller)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .condition(request.getCondition())
                .imageBase64(decodedImage)
                .active(true)
                .build();

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request, Authentication auth) {
        Product product = findByIdOrThrow(id);
        requireOwnerOrAdmin(product, auth);

        if (request.getImageBase64() != null && !request.getImageBase64().isBlank()) {
            ImageValidator.validate(request.getImageBase64());
            product.setImageBase64(request.getImageBase64());
        }

        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setCondition(request.getCondition());

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id, Authentication auth) {
        Product product = findByIdOrThrow(id);
        requireOwnerOrAdmin(product, auth);
        productRepository.delete(product);
    }

    /** Public so AdminService can reuse the same rating-aware mapping. */
    public ProductResponse toResponse(Product product) {
        RatingSummary sellerRating = ratingService.summarize(product.getSeller().getId());
        return ProductResponse.from(product, sellerRating);
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
