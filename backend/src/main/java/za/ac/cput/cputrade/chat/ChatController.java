package za.ac.cput.cputrade.chat;

import jakarta.validation.Valid;
import za.ac.cput.cputrade.chat.dto.ChatMessageRequest;
import za.ac.cput.cputrade.chat.dto.ChatMessageResponse;
import za.ac.cput.cputrade.chat.dto.ConversationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
