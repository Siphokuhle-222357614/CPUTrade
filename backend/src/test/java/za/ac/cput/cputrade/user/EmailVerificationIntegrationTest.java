package za.ac.cput.cputrade.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmailVerificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void studentMustVerifyEmailBeforeLoginAndTheLinkCanOnlyBeUsedOnce() throws Exception {
        String username = uniqueUsername();
        String token = "verification-token-" + UUID.randomUUID();
        User user = user(username, token, LocalDateTime.now().plusHours(1));
        userRepository.saveAndFlush(user);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"Password123!"}
                                """.formatted(username)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Verify your CPUT email address before signing in."));

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(token)))
                .andExpect(status().isNoContent());

        User verified = userRepository.findByUsername(username).orElseThrow();
        assertTrue(verified.isVerified());
        assertNull(verified.getEmailVerificationTokenHash());
        assertNull(verified.getEmailVerificationExpiresAt());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"Password123!"}
                                """.formatted(username)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void expiredTokensCannotVerifyAnAccount() throws Exception {
        String username = uniqueUsername();
        String token = "expired-token-" + UUID.randomUUID();
        userRepository.saveAndFlush(user(username, token, LocalDateTime.now().minusMinutes(1)));

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(token)))
                .andExpect(status().isBadRequest());

        User unverified = userRepository.findByUsername(username).orElseThrow();
        assertFalse(unverified.isVerified());
    }

    @Test
    void registeringAStudentCreatesAnUnverifiedAccountAndResendIsNonEnumerating() throws Exception {
        String username = uniqueUsername();
        String email = username + "@mycput.ac.za";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s","password":"Password123!","role":"STUDENT"}
                                """.formatted(username, email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.verified").value(false));

        User registered = userRepository.findByUsername(username).orElseThrow();
        assertFalse(registered.isVerified());
        assertTrue(registered.getEmailVerificationTokenHash().matches("[0-9a-f]{64}"));
        assertTrue(registered.getEmailVerificationExpiresAt().isAfter(LocalDateTime.now()));

        mockMvc.perform(post("/api/auth/verification/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s"}
                                """.formatted(email)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/verification/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"unknown@mycput.ac.za"}
                                """))
                .andExpect(status().isNoContent());
    }

    private User user(String username, String token, LocalDateTime expiresAt) throws Exception {
        String email = username + "@mycput.ac.za";
        return User.builder()
                .username(username)
                .email(email)
                .campusHandle(username + "@cputmarket.ac.za")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.STUDENT)
                .verified(false)
                .vendorApproved(true)
                .emailVerificationTokenHash(sha256(token))
                .emailVerificationExpiresAt(expiresAt)
                .emailVerificationSentAt(LocalDateTime.now())
                .build();
    }

    private String uniqueUsername() {
        return "verify_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private static String sha256(String token) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }
}
