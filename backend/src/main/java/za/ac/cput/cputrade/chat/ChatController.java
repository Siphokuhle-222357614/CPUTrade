package za.ac.cput.cputrade.chat;

import jakarta.validation.Valid;
import za.ac.cput.cputrade.chat.dto.ChatMessageEditRequest;
import za.ac.cput.cputrade.chat.dto.ChatMessageRequest;
import za.ac.cput.cputrade.chat.dto.ChatMessageResponse;
import za.ac.cput.cputrade.chat.dto.ConversationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /** Buyer starts (or re-opens) a thread with the seller about this listing. */
    @PostMapping("/api/products/{productId}/conversations")
    public ResponseEntity<ConversationResponse> startConversation(@PathVariable Long productId, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.startOrGetConversation(productId, auth));
    }

    /** All threads (as buyer or seller) for the current user — "My Chats". */
    @GetMapping("/api/conversations")
    public ResponseEntity<List<ConversationResponse>> myConversations(Authentication auth) {
        return ResponseEntity.ok(chatService.myConversations(auth));
    }

    @GetMapping("/api/conversations/{conversationId}")
    public ResponseEntity<ConversationResponse> getConversation(@PathVariable Long conversationId, Authentication auth) {
        return ResponseEntity.ok(chatService.getConversation(conversationId, auth));
    }

    @GetMapping("/api/conversations/{conversationId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> messages(@PathVariable Long conversationId, Authentication auth) {
        return ResponseEntity.ok(chatService.messages(conversationId, auth));
    }

    @PostMapping("/api/conversations/{conversationId}/messages")
    public ResponseEntity<ChatMessageResponse> postMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody ChatMessageRequest request,
            Authentication auth
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.postMessage(conversationId, request, auth));
    }

    /** The viewer has this conversation open — marks every message from the other party as read (seen tick). */
    @PostMapping("/api/conversations/{conversationId}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long conversationId, Authentication auth) {
        chatService.markRead(conversationId, auth);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/conversations/{conversationId}/messages/{messageId}")
    public ResponseEntity<ChatMessageResponse> editMessage(
            @PathVariable Long conversationId,
            @PathVariable Long messageId,
            @Valid @RequestBody ChatMessageEditRequest request,
            Authentication auth
    ) {
        return ResponseEntity.ok(chatService.editMessage(conversationId, messageId, request, auth));
    }

    /** Soft delete only — see {@code ChatService#deleteMessage}. */
    @DeleteMapping("/api/conversations/{conversationId}/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long conversationId,
            @PathVariable Long messageId,
            Authentication auth
    ) {
        chatService.deleteMessage(conversationId, messageId, auth);
        return ResponseEntity.noContent().build();
    }

    /** Frontend calls this on keystroke (debounced) — ephemeral, see TypingIndicatorService. */
    @PostMapping("/api/conversations/{conversationId}/typing")
    public ResponseEntity<Void> markTyping(@PathVariable Long conversationId, Authentication auth) {
        chatService.markTyping(conversationId, auth);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/conversations/{conversationId}/typing")
    public ResponseEntity<Map<String, Boolean>> isTyping(@PathVariable Long conversationId, Authentication auth) {
        return ResponseEntity.ok(Map.of("typing", chatService.isOtherPartyTyping(conversationId, auth)));
    }
}
