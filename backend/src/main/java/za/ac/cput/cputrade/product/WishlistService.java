package za.ac.cput.cputrade.product;

import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.product.dto.ProductResponse;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public WishlistService(WishlistItemRepository wishlistItemRepository, ProductRepository productRepository,
                            UserRepository userRepository, ProductService productService) {
        this.wishlistItemRepository = wishlistItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.productService = productService;
    }

    public List<ProductResponse> myWishlist(Authentication auth) {
        User user = currentUser(auth);
        return wishlistItemRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(item -> productService.toResponse(item.getProduct()))
                .toList();
    }

    /** So the frontend can mark hearts as filled across a whole grid without one call per card. */
    public List<Long> myWishlistIds(Authentication auth) {
        User user = currentUser(auth);
        return wishlistItemRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(item -> item.getProduct().getId())
                .toList();
    }

    /** Idempotent — wishlisting something already on the list just confirms it's there. */
    @Transactional
    public void add(Long productId, Authentication auth) {
        User user = currentUser(auth);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ApiException.notFound("Listing not found"));

        if (product.getSeller().getId().equals(user.getId())) {
            throw ApiException.badRequest("You can't wishlist your own listing");
        }
        if (wishlistItemRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            return;
        }

        wishlistItemRepository.save(WishlistItem.builder().user(user).product(product).build());
    }

    /** Idempotent — removing something not on the list is a no-op, not an error. */
    @Transactional
    public void remove(Long productId, Authentication auth) {
        User user = currentUser(auth);
        wishlistItemRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> ApiException.unauthorized("Unknown user"));
    }
}
