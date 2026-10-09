package za.ac.cput.cputrade.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /**
     * Plain SELECT ... FOR UPDATE rather than JPA's {@code @Lock}: Hibernate emits
     * {@code FOR UPDATE OF alias}, which MariaDB (e.g. XAMPP) rejects.
     * Loads the row with a database write lock, so two simultaneous sale actions on the same
     * listing run one after the other instead of both reading the same stock count.
     */
    @Query(value = "SELECT * FROM products WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    /** Admin moderation view — includes inactive listings (US6.3). */
    List<Product> findAllByOrderByCreatedAtDesc();

    /** A seller's own "My Listings" / business dashboard view — includes their own inactive listings too. */
    List<Product> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    /** Shown on a user's public profile — active listings only. */
    long countBySellerIdAndActiveTrue(Long sellerId);

    /** Shown on a user's public profile as a trust signal — "N completed sales". */
    long countBySellerIdAndSoldTrue(Long sellerId);

    /** Marketplace "recent activity" ticker — social proof that the platform is actually alive. */
    List<Product> findTop10BySoldTrueOrderBySoldAtDesc();
}
