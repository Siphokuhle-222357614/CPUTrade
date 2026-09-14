package za.ac.cput.cputrade.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Everyone watching a given listing — used to notify on a price drop or a sale. */
    List<WishlistItem> findByProductId(Long productId);

    long countByProductId(Long productId);

    Optional<WishlistItem> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);

    /** So a hard-deleted listing doesn't leave orphaned wishlist rows behind an FK constraint. */
    void deleteByProductId(Long productId);
}
