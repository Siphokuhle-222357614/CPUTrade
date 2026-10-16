package za.ac.cput.cputrade.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;
import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.user.notification.EmailNotifier;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EmailVerificationServiceTest {

    private UserRepository userRepository;
    private EmailNotifier emailNotifier;
    private EmailVerificationService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        emailNotifier = mock(EmailNotifier.class);
        service = new EmailVerificationService(userRepository, emailNotifier, "http://localhost:5173", 1440, 15);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsRandomLinkAndPersistsOnlyItsHash() throws Exception {
        User user = student();
        LocalDateTime before = LocalDateTime.now();

        service.createAndSendVerificationEmail(user);

        var urlCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailNotifier).sendVerificationEmail(eq(user), urlCaptor.capture());
        String token = UriComponentsBuilder.fromUriString(urlCaptor.getValue())
                .build()
                .getQueryParams()
                .getFirst("token");

        assertNotNull(token);
        assertEquals(43, token.length());
        assertNotEquals(token, user.getEmailVerificationTokenHash());
        assertEquals(sha256(token), user.getEmailVerificationTokenHash());
        assertTrue(user.getEmailVerificationExpiresAt().isAfter(before.plusMinutes(1439)));
        assertNotNull(user.getEmailVerificationSentAt());
    }

    @Test
    void successfulVerificationConsumesTheTokenAtomically() throws Exception {
        when(userRepository.consumeEmailVerificationToken(anyString(), any(LocalDateTime.class))).thenReturn(1);

        assertDoesNotThrow(() -> service.verifyEmail("valid-token"));

        verify(userRepository).consumeEmailVerificationToken(
                eq(sha256("valid-token")), any(LocalDateTime.class));
    }

    @Test
    void invalidOrExpiredVerificationTokensAreRejected() throws Exception {
        when(userRepository.consumeEmailVerificationToken(anyString(), any(LocalDateTime.class))).thenReturn(0);

        ApiException exception = assertThrows(ApiException.class, () -> service.verifyEmail("expired-token"));

        assertEquals("This verification link is invalid or expired. Request a new link.", exception.getMessage());
    }

    @Test
    void resendDoesNotSendAgainDuringTheCooldown() {
        User user = student();
        user.setEmailVerificationSentAt(LocalDateTime.now());
        when(userRepository.findByEmailIgnoreCaseForVerificationUpdate(user.getEmail())).thenReturn(Optional.of(user));

        service.resendVerificationEmail(user.getEmail());

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(emailNotifier);
    }

    @Test
    void resendSendsANewLinkAfterTheCooldownAndIgnoresUnknownAddresses() {
        User user = student();
        user.setEmailVerificationSentAt(LocalDateTime.now().minusMinutes(16));
        when(userRepository.findByEmailIgnoreCaseForVerificationUpdate(user.getEmail())).thenReturn(Optional.of(user));

        service.resendVerificationEmail(user.getEmail());
        service.resendVerificationEmail("missing@mycput.ac.za");

        verify(emailNotifier).sendVerificationEmail(eq(user), anyString());
        verify(userRepository).findByEmailIgnoreCaseForVerificationUpdate("missing@mycput.ac.za");
    }

    private static User student() {
        return User.builder()
                .username("student")
                .email("student@mycput.ac.za")
                .campusHandle("student@cputmarket.ac.za")
                .passwordHash("not-used")
                .role(Role.STUDENT)
                .verified(false)
                .build();
    }

    private static String sha256(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }
}
