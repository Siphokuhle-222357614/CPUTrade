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
    private String body;
    private LocationSuggestion locationSuggestion;
    private LocalDateTime createdAt;

    public static ChatMessageResponse from(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .body(message.getBody())
                .locationSuggestion(message.getLocationSuggestion())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
