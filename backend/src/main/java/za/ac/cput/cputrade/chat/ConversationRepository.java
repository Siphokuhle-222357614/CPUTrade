package za.ac.cput.cputrade.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByProductIdAndBuyerId(Long productId, Long buyerId);

    List<Conversation> findByBuyerIdOrSellerIdOrderByCreatedAtDesc(Long buyerId, Long sellerId);
}
