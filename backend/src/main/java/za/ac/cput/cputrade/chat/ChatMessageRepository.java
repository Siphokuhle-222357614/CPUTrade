package za.ac.cput.cputrade.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    /** Messages in this thread sent by someone else, not yet delivered/read — used to update ticks. */
    List<ChatMessage> findByConversationIdAndSenderIdNotAndDeliveredAtIsNull(Long conversationId, Long senderId);

    List<ChatMessage> findByConversationIdAndSenderIdNotAndReadAtIsNull(Long conversationId, Long senderId);

    /** So a deleted listing's conversations don't leave orphaned messages behind an FK constraint. */
    void deleteByConversationIdIn(List<Long> conversationIds);
}
