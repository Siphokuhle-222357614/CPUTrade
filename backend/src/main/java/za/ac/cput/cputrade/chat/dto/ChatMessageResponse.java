package za.ac.cput.cputrade.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import za.ac.cput.cputrade.chat.ChatMessage;
import za.ac.cput.cputrade.chat.LocationSuggestion;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    /** Null when {@code deleted} is true — the real content never leaves the backend once deleted. */
    private String body;
    private LocationSuggestion locationSuggestion;
    private LocalDateTime createdAt;
    private boolean edited;
    private LocalDateTime editedAt;
    private boolean deleted;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;

    public static ChatMessageResponse from(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .body(message.isDeleted() ? null : message.getBody())
                .locationSuggestion(message.isDeleted() ? null : message.getLocationSuggestion())
                .createdAt(message.getCreatedAt())
                .edited(message.isEdited())
                .editedAt(message.getEditedAt())
                .deleted(message.isDeleted())
                .deliveredAt(message.getDeliveredAt())
                .readAt(message.getReadAt())
                .build();
    }
}
