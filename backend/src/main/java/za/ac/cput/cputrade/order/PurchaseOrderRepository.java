package za.ac.cput.cputrade.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query(value = "SELECT * FROM marketplace_orders WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<PurchaseOrder> findByIdForUpdate(@Param("id") Long id);

    List<PurchaseOrder> findByBuyerIdOrSellerIdOrderByCreatedAtDesc(Long buyerId, Long sellerId);

    boolean existsByProductId(Long productId);

    long countByProductId(Long productId);

    boolean existsByProductIdAndBuyerId(Long productId, Long buyerId);

    boolean existsByProductIdAndBuyerIdAndReceivedAtIsNotNull(Long productId, Long buyerId);
}
