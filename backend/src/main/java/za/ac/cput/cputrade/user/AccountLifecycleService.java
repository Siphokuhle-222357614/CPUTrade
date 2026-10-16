package za.ac.cput.cputrade.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.cputrade.bulletin.BulletinPostRepository;
import za.ac.cput.cputrade.chat.ChatMessageRepository;
import za.ac.cput.cputrade.chat.Conversation;
import za.ac.cput.cputrade.chat.ConversationRepository;
import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.notification.NotificationRepository;
import za.ac.cput.cputrade.product.Product;
import za.ac.cput.cputrade.product.ProductRepository;
import za.ac.cput.cputrade.product.SavedSearchRepository;
import za.ac.cput.cputrade.product.WishlistItemRepository;
import za.ac.cput.cputrade.push.PushSubscriptionRepository;
import za.ac.cput.cputrade.rating.Rating;
import za.ac.cput.cputrade.rating.RatingRepository;
import za.ac.cput.cputrade.trust.UserBlockRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AccountLifecycleService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductRepository productRepository;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final SavedSearchRepository savedSearchRepository;
    private final NotificationRepository notificationRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final UserBlockRepository userBlockRepository;
    private final BulletinPostRepository bulletinPostRepository;
    private final RatingRepository ratingRepository;

    public AccountLifecycleService(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   ProductRepository productRepository,
                                   ConversationRepository conversationRepository,
                                   ChatMessageRepository chatMessageRepository,
                                   WishlistItemRepository wishlistItemRepository,
                                   SavedSearchRepository savedSearchRepository,
                                   NotificationRepository notificationRepository,
                                   PushSubscriptionRepository pushSubscriptionRepository,
                                   UserBlockRepository userBlockRepository,
                                   BulletinPostRepository bulletinPostRepository,
                                   RatingRepository ratingRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.productRepository = productRepository;
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.wishlistItemRepository = wishlistItemRepository;
        this.savedSearchRepository = savedSearchRepository;
        this.notificationRepository = notificationRepository;
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.userBlockRepository = userBlockRepository;
        this.bulletinPostRepository = bulletinPostRepository;
        this.ratingRepository = ratingRepository;
    }

    /**
     * Removes direct account identifiers and private account data while
     * retaining sale, rating-score, report, and appeal records by user ID.
     *
     * @return product-image URLs to remove after the database transaction commits
     */
    @Transactional
    public List<String> anonymizeAccount(String username, String currentPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.unauthorized("Authenticated account not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw ApiException.forbidden("Current password is incorrect");
        }

        Long userId = user.getId();
        List<String> imageUrls = new ArrayList<>();
        List<Product> products = productRepository.findBySellerIdOrderByCreatedAtDesc(userId);
        for (Product product : products) {
            if (product.getImageUrls() != null) {
                imageUrls.addAll(product.getImageUrls());
                product.getImageUrls().clear();
            }
            product.setDescription(null);
            product.setOpenToSwap(false);
            product.setSwapPreferences(null);
            product.setActive(false);
        }
        productRepository.saveAll(products);

        List<Conversation> conversations =
                conversationRepository.findByBuyerIdOrSellerIdOrderByCreatedAtDesc(userId, userId);
        if (!conversations.isEmpty()) {
            List<Long> conversationIds = conversations.stream().map(Conversation::getId).toList();
            chatMessageRepository.deleteByConversationIdIn(conversationIds);
            conversationRepository.deleteAll(conversations);
        }

        bulletinPostRepository.deleteByAuthorId(userId);
        wishlistItemRepository.deleteByUserId(userId);
        savedSearchRepository.deleteByUserId(userId);
        notificationRepository.deleteByRecipientId(userId);
        pushSubscriptionRepository.deleteByUserId(userId);
        userBlockRepository.deleteByBlockerIdOrBlockedId(userId, userId);

        List<Rating> ratingsGiven = ratingRepository.findByRaterId(userId);
        ratingsGiven.forEach(rating -> rating.setComment(null));
        ratingRepository.saveAll(ratingsGiven);

        String randomId = UUID.randomUUID().toString().replace("-", "");
        user.setUsername("deleted-" + randomId);
        user.setEmail("deleted+" + randomId + "@deleted.invalid");
        user.setCampusHandle("deleted-" + randomId + "@cputmarket.ac.za");
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setVerified(false);
        user.setEmailVerificationTokenHash(null);
        user.setEmailVerificationExpiresAt(null);
        user.setEmailVerificationSentAt(null);
        user.setVendorApproved(false);
        user.setAccountStatus(AccountStatus.DELETED);
        user.setSuspensionReason(null);
        user.setSuspendedUntil(null);
        user.setLastActiveAt(null);
        userRepository.saveAndFlush(user);

        return imageUrls;
    }
}