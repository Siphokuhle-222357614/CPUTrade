package za.ac.cput.cputrade.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AccountLifecycleServiceTest {

    private static final String USERNAME = "student_1";
    private static final String PASSWORD = "correct-password";
    private static final String PASSWORD_HASH = "existing-password-hash";
    private static final Long USER_ID = 17L;

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private ProductRepository productRepository;
    private ConversationRepository conversationRepository;
    private ChatMessageRepository chatMessageRepository;
    private WishlistItemRepository wishlistItemRepository;
    private SavedSearchRepository savedSearchRepository;
    private NotificationRepository notificationRepository;
    private PushSubscriptionRepository pushSubscriptionRepository;
    private UserBlockRepository userBlockRepository;
    private BulletinPostRepository bulletinPostRepository;
    private RatingRepository ratingRepository;
    private AccountLifecycleService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        productRepository = mock(ProductRepository.class);
        conversationRepository = mock(ConversationRepository.class);
        chatMessageRepository = mock(ChatMessageRepository.class);
        wishlistItemRepository = mock(WishlistItemRepository.class);
        savedSearchRepository = mock(SavedSearchRepository.class);
        notificationRepository = mock(NotificationRepository.class);
        pushSubscriptionRepository = mock(PushSubscriptionRepository.class);
        userBlockRepository = mock(UserBlockRepository.class);
        bulletinPostRepository = mock(BulletinPostRepository.class);
        ratingRepository = mock(RatingRepository.class);
        service = new AccountLifecycleService(userRepository, passwordEncoder, productRepository,
                conversationRepository, chatMessageRepository, wishlistItemRepository,
                savedSearchRepository, notificationRepository, pushSubscriptionRepository,
                userBlockRepository, bulletinPostRepository, ratingRepository);

        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("rotated-password-hash");
    }

    @Test
    void anonymizesIdentityAndPrivateDataWhileRetainingSaleAndRatingHistory() {
        User user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .email("student@mycput.ac.za")
                .campusHandle("student@cputmarket.ac.za")
                .passwordHash(PASSWORD_HASH)
                .role(Role.STUDENT)
                .verified(true)
                .vendorApproved(true)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
        Product product = Product.builder()
                .id(28L)
                .seller(user)
                .title("Scientific calculator")
                .description("A personal description")
                .imageUrls(new ArrayList<>(List.of("/uploads/products/photo.jpg")))
                .active(true)
                .openToSwap(true)
                .swapPreferences("Looking for a textbook")
                .build();
        Rating rating = Rating.builder()
                .id(43L)
                .rater(user)
                .score(5)
                .comment("A private comment")
                .build();
        Conversation conversation = Conversation.builder().id(62L).build();

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(productRepository.findBySellerIdOrderByCreatedAtDesc(USER_ID)).thenReturn(List.of(product));
        when(conversationRepository.findByBuyerIdOrSellerIdOrderByCreatedAtDesc(USER_ID, USER_ID))
                .thenReturn(List.of(conversation));
        when(ratingRepository.findByRaterId(USER_ID)).thenReturn(List.of(rating));

        List<String> images = service.anonymizeAccount(USERNAME, PASSWORD);

        assertEquals(List.of("/uploads/products/photo.jpg"), images);
        assertEquals(AccountStatus.DELETED, user.getAccountStatus());
        assertEquals("Deleted account", user.getDisplayName());
        assertNotEquals(USERNAME, user.getUsername());
        assertNotEquals("student@mycput.ac.za", user.getEmail());
        assertNotEquals("student@cputmarket.ac.za", user.getCampusHandle());
        assertEquals("rotated-password-hash", user.getPasswordHash());
        assertFalse(user.isVerified());
        assertFalse(user.isVendorApproved());

        assertFalse(product.isActive());
        assertNull(product.getDescription());
        assertFalse(product.isOpenToSwap());
        assertNull(product.getSwapPreferences());
        assertTrue(product.getImageUrls().isEmpty());
        assertEquals("Scientific calculator", product.getTitle());

        assertEquals(5, rating.getScore());
        assertNull(rating.getComment());
        verify(chatMessageRepository).deleteByConversationIdIn(List.of(62L));
        verify(conversationRepository).deleteAll(List.of(conversation));
        verify(bulletinPostRepository).deleteByAuthorId(USER_ID);
        verify(wishlistItemRepository).deleteByUserId(USER_ID);
        verify(savedSearchRepository).deleteByUserId(USER_ID);
        verify(notificationRepository).deleteByRecipientId(USER_ID);
        verify(pushSubscriptionRepository).deleteByUserId(USER_ID);
        verify(userBlockRepository).deleteByBlockerIdOrBlockedId(USER_ID, USER_ID);
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void rejectsAnIncorrectPasswordBeforeChangingAccountData() {
        User user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .passwordHash(PASSWORD_HASH)
                .role(Role.STUDENT)
                .build();
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", PASSWORD_HASH)).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.anonymizeAccount(USERNAME, "wrong-password"));

        assertEquals(403, exception.getStatus().value());
        verify(userRepository, never()).saveAndFlush(any(User.class));
        verifyNoInteractions(productRepository, conversationRepository, chatMessageRepository,
                wishlistItemRepository, savedSearchRepository, notificationRepository,
                pushSubscriptionRepository, userBlockRepository, bulletinPostRepository, ratingRepository);
    }
}