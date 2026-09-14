package za.ac.cput.cputrade.chat;

import za.ac.cput.cputrade.chat.dto.ChatMessageEditRequest;
import za.ac.cput.cputrade.chat.dto.ChatMessageRequest;
import za.ac.cput.cputrade.chat.dto.ChatMessageResponse;
import za.ac.cput.cputrade.chat.dto.ConversationResponse;
import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.notification.NotificationService;
import za.ac.cput.cputrade.notification.NotificationType;
import za.ac.cput.cputrade.product.Product;
import za.ac.cput.cputrade.product.ProductRepository;
import za.ac.cput.cputrade.trust.BlockService;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final BlockService blockService;
    private final TypingIndicatorService typingIndicatorService;

    public ChatService(ConversationRepository conversationRepository, ChatMessageRepository chatMessageRepository,
                        ProductRepository productRepository, UserRepository userRepository,
                        NotificationService notificationService, BlockService blockService,
                        TypingIndicatorService typingIndicatorService) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.blockService = blockService;
        this.typingIndicatorService = typingIndicatorService;
    }

    /** Shared by both entry points into a conversation — a block in either direction stops messaging. */
    private void checkNotBlocked(User a, User b) {
        if (blockService.hasBlocked(a.getId(), b.getId())) {
            throw ApiException.forbidden("You have blocked this user. Unblock them to send messages.");
        }
        if (blockService.hasBlocked(b.getId(), a.getId())) {
            throw ApiException.forbidden("You can't message this user.");
        }
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
        checkNotBlocked(buyer, product.getSeller());

        Optional<Conversation> existing = conversationRepository.findByProductIdAndBuyerId(productId, buyer.getId());
        // Block starting a *new* thread about something already sold, but let a buyer who
        // was already chatting about it (e.g. to arrange pickup) keep using that thread.
        if (existing.isEmpty() && product.isSold()) {
            throw ApiException.badRequest("This item has already been sold");
        }

        Conversation conversation = existing.orElseGet(() -> conversationRepository.save(
                Conversation.builder().product(product).buyer(buyer).seller(product.getSeller()).build()
        ));

        return ConversationResponse.from(conversation);
    }

    public List<ConversationResponse> myConversations(Authentication auth) {
        User user = currentUser(auth);
        return conversationRepository.findByBuyerIdOrSellerIdOrderByCreatedAtDesc(user.getId(), user.getId())
                .stream().map(ConversationResponse::from).toList();
    }

    public ConversationResponse getConversation(Long conversationId, Authentication auth) {
        return ConversationResponse.from(requireParticipant(conversationId, auth));
    }

    @Transactional
    public List<ChatMessageResponse> messages(Long conversationId, Authentication auth) {
        Conversation conversation = requireParticipant(conversationId, auth);
        User viewer = currentUser(auth);

        // Fetching the thread is what "delivered" means in a poll-based chat
        // (no push channel to know the moment their client actually got it) —
        // any message from the other party not yet marked delivered becomes
        // delivered now, the single check becoming a double check.
        List<ChatMessage> undelivered = chatMessageRepository
                .findByConversationIdAndSenderIdNotAndDeliveredAtIsNull(conversationId, viewer.getId());
        if (!undelivered.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            undelivered.forEach(m -> m.setDeliveredAt(now));
            chatMessageRepository.saveAll(undelivered);
        }

        return chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId())
                .stream().map(ChatMessageResponse::from).toList();
    }

    /** The viewer has the conversation open — every message from the other party becomes "seen" (blue double check). */
    @Transactional
    public void markRead(Long conversationId, Authentication auth) {
        Conversation conversation = requireParticipant(conversationId, auth);
        User viewer = currentUser(auth);

        List<ChatMessage> unread = chatMessageRepository
                .findByConversationIdAndSenderIdNotAndReadAtIsNull(conversation.getId(), viewer.getId());
        if (unread.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();
        unread.forEach(m -> {
            m.setReadAt(now);
            if (m.getDeliveredAt() == null) m.setDeliveredAt(now); // seen implies delivered
        });
        chatMessageRepository.saveAll(unread);
    }

    @Transactional
    public ChatMessageResponse editMessage(Long conversationId, Long messageId, ChatMessageEditRequest request, Authentication auth) {
        User user = currentUser(auth);
        ChatMessage message = requireOwnMessage(conversationId, messageId, user);
        if (message.isDeleted()) {
            throw ApiException.badRequest("Can't edit a deleted message");
        }
        message.setBody(request.getBody());
        message.setEdited(true);
        message.setEditedAt(LocalDateTime.now());
        return ChatMessageResponse.from(chatMessageRepository.save(message));
    }

    /**
     * Soft delete — {@code body} is left untouched in the database (see
     * {@link ChatMessage#isDeleted()}); only the {@code deleted} flag is
     * set, which is all {@link ChatMessageResponse} checks before deciding
     * whether to expose the real content.
     */
    @Transactional
    public void deleteMessage(Long conversationId, Long messageId, Authentication auth) {
        User user = currentUser(auth);
        ChatMessage message = requireOwnMessage(conversationId, messageId, user);
        message.setDeleted(true);
        chatMessageRepository.save(message);
    }

    public void markTyping(Long conversationId, Authentication auth) {
        Conversation conversation = requireParticipant(conversationId, auth);
        User user = currentUser(auth);
        typingIndicatorService.markTyping(conversation.getId(), user.getId());
    }

    public boolean isOtherPartyTyping(Long conversationId, Authentication auth) {
        Conversation conversation = requireParticipant(conversationId, auth);
        User user = currentUser(auth);
        Long otherId = conversation.getBuyer().getId().equals(user.getId())
                ? conversation.getSeller().getId()
                : conversation.getBuyer().getId();
        return typingIndicatorService.isTyping(conversation.getId(), otherId);
    }

    private ChatMessage requireOwnMessage(Long conversationId, Long messageId, User user) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> ApiException.notFound("Message not found"));
        if (!message.getConversation().getId().equals(conversationId)) {
            throw ApiException.notFound("Message not found");
        }
        if (!message.getSender().getId().equals(user.getId())) {
            throw ApiException.forbidden("You may only edit or delete your own messages");
        }
        return message;
    }

    @Transactional
    public ChatMessageResponse postMessage(Long conversationId, ChatMessageRequest request, Authentication auth) {
        Conversation conversation = requireParticipant(conversationId, auth);
        User sender = currentUser(auth);
        User recipient = conversation.getBuyer().getId().equals(sender.getId())
                ? conversation.getSeller()
                : conversation.getBuyer();
        checkNotBlocked(sender, recipient);

        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .body(request.getBody())
                .locationSuggestion(request.getLocationSuggestion())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        // US4.3: notify whichever participant didn't send this message.
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
