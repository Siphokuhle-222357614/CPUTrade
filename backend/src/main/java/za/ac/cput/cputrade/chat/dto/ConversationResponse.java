package za.ac.cput.cputrade.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import za.ac.cput.cputrade.chat.Conversation;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ConversationResponse {
    private Long id;
    private Long productId;
    private String productTitle;
    private Long buyerId;
    private String buyerUsername;
    private Long sellerId;
    private String sellerUsername;
    private LocalDateTime createdAt;

    public static ConversationResponse from(Conversation conversation) {
        return ConversationResponse.builder()
                .id(conversation.getId())
                .productId(conversation.getProduct().getId())
                .productTitle(conversation.getProduct().getTitle())
                .buyerId(conversation.getBuyer().getId())
                .buyerUsername(conversation.getBuyer().getUsername())
                .sellerId(conversation.getSeller().getId())
                .sellerUsername(conversation.getSeller().getUsername())
                .createdAt(conversation.getCreatedAt())
                .build();
    }
}
