package za.ac.cput.cputrade.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.user.notification.EmailNotifier;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class EmailVerificationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailNotifier emailNotifier;
    private final String frontendBaseUrl;
    private final long expirationMinutes;
    private final long resendCooldownMinutes;

    public EmailVerificationService(
            UserRepository userRepository,
            EmailNotifier emailNotifier,
            @Value("${app.frontend.base-url:http://localhost:5173}") String frontendBaseUrl,
            @Value("${app.auth.email-verification-expiration-minutes:1440}") long expirationMinutes,
            @Value("${app.auth.email-verification-resend-cooldown-minutes:15}") long resendCooldownMinutes
    ) {
        this.userRepository = userRepository;
        this.emailNotifier = emailNotifier;
        this.frontendBaseUrl = frontendBaseUrl;
        this.expirationMinutes = expirationMinutes;
        this.resendCooldownMinutes = resendCooldownMinutes;
    }

    @Transactional
    public void createAndSendVerificationEmail(User user) {
        issueToken(user, LocalDateTime.now());
    }

    @Transactional
    public void verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw invalidToken();
        }

        String tokenHash = hashToken(token);
        LocalDateTime now = LocalDateTime.now();
        int verified = userRepository.consumeEmailVerificationToken(tokenHash, now);
        if (verified != 1) {
            throw invalidToken();
        }
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        Optional<User> maybeUser = userRepository.findByEmailIgnoreCaseForVerificationUpdate(email);
        if (maybeUser.isEmpty()) {
            return;
        }

        User user = maybeUser.get();
        if (user.getRole() != Role.STUDENT || user.isVerified()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastSentAt = user.getEmailVerificationSentAt();
        if (lastSentAt != null && lastSentAt.isAfter(now.minusMinutes(resendCooldownMinutes))) {
            return;
        }

        issueToken(user, now);
    }

    private void issueToken(User user, LocalDateTime now) {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        user.setEmailVerificationTokenHash(hashToken(token));
        user.setEmailVerificationExpiresAt(now.plusMinutes(expirationMinutes));
        user.setEmailVerificationSentAt(now);
        User saved = userRepository.save(user);

        String verificationUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl)
                .path("/verify-email")
                .queryParam("token", token)
                .build()
                .toUriString();
        emailNotifier.sendVerificationEmail(saved, verificationUrl);
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required for email verification tokens", e);
        }
    }

    private ApiException invalidToken() {
        return ApiException.badRequest("This verification link is invalid or expired. Request a new link.");
    }
}
