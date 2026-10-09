package za.ac.cput.cputrade.user;

import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.security.JwtUtil;
import za.ac.cput.cputrade.user.dto.LoginRequest;
import za.ac.cput.cputrade.user.dto.RegisterRequest;
import za.ac.cput.cputrade.user.notification.EmailNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository users;
    private PasswordEncoder encoder;
    private AuthService service;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        encoder = new BCryptPasswordEncoder();
        JwtUtil jwt = new JwtUtil("a-test-secret-that-is-long-enough-for-hmac-sha-256", 60_000);
        service = new AuthService(users, encoder, jwt, mock(EmailNotifier.class), mock(AccountStatusService.class));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static RegisterRequest request(String username, String email, String role) {
        RegisterRequest r = new RegisterRequest();
        r.setUsername(username);
        r.setEmail(email);
        r.setPassword("Password123!");
        r.setRole(role);
        return r;
    }

    private User savedUser() {
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        return saved.getValue();
    }

    @Test
    void studentWithANonCputEmailIsRejected() {
        ApiException e = assertThrows(ApiException.class, () -> service.register(request("thabo_s", "thabo@gmail.com", "STUDENT")));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        verify(users, never()).save(any());
    }

    @Test
    void studentWithACputEmailIsAccepted() {
        service.register(request("thabo_s", "thabo_s@mycput.ac.za", "STUDENT"));
        User user = savedUser();
        assertEquals(Role.STUDENT, user.getRole());
        assertTrue(user.isVendorApproved());
    }

    @Test
    void lookalikeCputDomainsAreRejected() {
        assertThrows(ApiException.class, () -> service.register(request("thabo_s", "thabo@mycput.ac.za.evil.com", "STUDENT")));
        assertThrows(ApiException.class, () -> service.register(request("thabo_s", "thabo@evilmycput.ac.za", "STUDENT")));
    }

    @Test
    void vendorsNeedAdminApprovalBeforeSelling() {
        service.register(request("campusprint", "hello@campusprint.example", "VENDOR"));
        User user = savedUser();
        assertEquals(Role.VENDOR, user.getRole());
        assertFalse(user.isVendorApproved());
    }

    @Test
    void passwordIsStoredHashedNeverAsPlainText() {
        service.register(request("thabo_s", "thabo_s@mycput.ac.za", "STUDENT"));
        User user = savedUser();
        assertNotEquals("Password123!", user.getPasswordHash());
        assertTrue(encoder.matches("Password123!", user.getPasswordHash()));
    }

    @Test
    void duplicateUsernameIsRejected() {
        when(users.existsByUsername("thabo_s")).thenReturn(true);
        ApiException e = assertThrows(ApiException.class, () -> service.register(request("thabo_s", "thabo_s@mycput.ac.za", "STUDENT")));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
    }

    @Test
    void loginWithTheWrongPasswordIsUnauthorized() {
        User user = User.builder().username("thabo_s").passwordHash(encoder.encode("Password123!")).role(Role.STUDENT).build();
        when(users.findByUsername("thabo_s")).thenReturn(Optional.of(user));
        LoginRequest login = new LoginRequest();
        login.setUsername("thabo_s");
        login.setPassword("wrong-password");
        ApiException e = assertThrows(ApiException.class, () -> service.login(login));
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
    }
}
