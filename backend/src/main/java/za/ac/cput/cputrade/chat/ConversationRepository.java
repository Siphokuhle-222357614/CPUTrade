package za.ac.cput.cputrade.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByProductIdAndBuyerId(Long productId, Long buyerId);

    List<Conversation> findByBuyerIdOrSellerIdOrderByCreatedAtDesc(Long buyerId, Long sellerId);

    /** Everyone who has messaged the seller about this listing — the "mark as sold to" candidate list. */
    List<Conversation> findByProductIdOrderByCreatedAtDesc(Long productId);

    /** So a deleted listing doesn't leave orphaned conversations behind an FK constraint. */
    void deleteByProductId(Long productId);
}
