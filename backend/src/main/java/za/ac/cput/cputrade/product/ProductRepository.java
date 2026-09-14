package za.ac.cput.cputrade.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /** Admin moderation view — includes inactive listings (US6.3). */
    List<Product> findAllByOrderByCreatedAtDesc();

    /** A seller's own "My Listings" / business dashboard view — includes their own inactive listings too. */
    List<Product> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    /** Shown on a user's public profile — active listings only. */
    long countBySellerIdAndActiveTrue(Long sellerId);
}
