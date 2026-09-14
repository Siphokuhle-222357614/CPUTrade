package za.ac.cput.cputrade.product;

import za.ac.cput.cputrade.product.dto.ProductResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** US: "watch a listing" — every route here requires auth (covered by SecurityConfig's default anyRequest rule). */
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> myWishlist(Authentication auth) {
        return ResponseEntity.ok(wishlistService.myWishlist(auth));
    }

    /** Lightweight — just the product ids, so the frontend can mark hearts across a grid in one call. */
    @GetMapping("/ids")
    public ResponseEntity<List<Long>> myWishlistIds(Authentication auth) {
        return ResponseEntity.ok(wishlistService.myWishlistIds(auth));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Void> add(@PathVariable Long productId, Authentication auth) {
        wishlistService.add(productId, auth);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(@PathVariable Long productId, Authentication auth) {
        wishlistService.remove(productId, auth);
        return ResponseEntity.noContent().build();
    }
}
