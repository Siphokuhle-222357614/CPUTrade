package za.ac.cput.cputrade.chat;

import za.ac.cput.cputrade.chat.dto.ChatMessageRequest;
import za.ac.cput.cputrade.chat.dto.ChatMessageResponse;
import za.ac.cput.cputrade.chat.dto.ConversationResponse;
import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.notification.NotificationService;
import za.ac.cput.cputrade.notification.NotificationType;
import za.ac.cput.cputrade.product.Product;
import za.ac.cput.cputrade.product.ProductRepository;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ChatService(ConversationRepository conversationRepository, ChatMessageRepository chatMessageRepository,
                        ProductRepository productRepository, UserRepository userRepository,
                        NotificationService notificationService) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /** A buyer messaging a seller for the first time about a listing starts (or reuses) the thread. */
    @Transactional
    public ConversationResponse startOrGetConversation(Long productId, Authentication auth) {
        User buyer = currentUser(auth);
        Product product = productRepository.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> ApiException.notFound("Listing not found"));

        if (product.getSeller().getId().equals(buyer.getId())) {
            throw ApiException.badRequest("You cannot message yourself about your own listing");
        }

        Conversation conversation = conversationRepository.findByProductIdAndBuyerId(productId, buyer.getId())
                .orElseGet(() -> conversationRepository.save(
                        Conversation.builder().product(product).buyer(buyer).seller(product.getSeller()).build()
                ));

        return ConversationResponse.from(conversation);
    }

    public List<ConversationResponse> myConversations(Authentication auth) {
        User user = currentUser(auth);
        return conversationRepository.findByBuyerIdOrSellerIdOrderByCreatedAtDesc(user.getId(), user.getId())
                .stream().map(ConversationResponse::from).toList();
    }

    public List<ChatMessageResponse> messages(Long conversationId, Authentication auth) {
        Conversation conversation = requireParticipant(conversationId, auth);
        return chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId())
                .stream().map(ChatMessageResponse::from).toList();
    }

    @Transactional
    public ChatMessageResponse postMessage(Long conversationId, ChatMessageRequest request, Authentication auth) {
        Conversation conversation = requireParticipant(conversationId, auth);
        User sender = currentUser(auth);

        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .body(request.getBody())
                .locationSuggestion(request.getLocationSuggestion())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        // US4.3: notify whichever participant didn't send this message.
        User recipient = conversation.getBuyer().getId().equals(sender.getId())
                ? conversation.getSeller()
                : conversation.getBuyer();
        notificationService.create(recipient, NotificationType.NEW_MESSAGE,
                sender.getUsername() + " sent you a message about \"" + conversation.getProduct().getTitle() + "\"",
                "/chats/" + conversation.getId());

        return ChatMessageResponse.from(saved);
    }

    private Conversation requireParticipant(Long conversationId, Authentication auth) {
        User user = currentUser(auth);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> ApiException.notFound("Conversation not found"));

        boolean participant = conversation.getBuyer().getId().equals(user.getId())
                || conversation.getSeller().getId().equals(user.getId());
        if (!participant) {
            throw ApiException.forbidden("You are not part of this conversation");
        }
        return conversation;
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> ApiException.unauthorized("Unknown user"));
    }
}
