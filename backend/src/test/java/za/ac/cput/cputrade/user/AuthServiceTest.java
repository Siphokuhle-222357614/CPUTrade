package za.ac.cput.cputrade.user;

import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.security.JwtUtil;
import za.ac.cput.cputrade.user.dto.LoginRequest;
import za.ac.cput.cputrade.user.dto.RegisterRequest;
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
    private EmailVerificationService emailVerificationService;
    private AccountStatusService accountStatusService;
    private AuthService service;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        encoder = new BCryptPasswordEncoder();
        JwtUtil jwt = new JwtUtil("a-test-secret-that-is-long-enough-for-hmac-sha-256", 60_000);
        emailVerificationService = mock(EmailVerificationService.class);
        accountStatusService = mock(AccountStatusService.class);
        when(accountStatusService.resolveStatus(any())).thenAnswer(inv -> inv.getArgument(0));
        service = new AuthService(users, encoder, jwt, emailVerificationService, accountStatusService);
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
        assertFalse(user.isVerified());
        verify(emailVerificationService).createAndSendVerificationEmail(user);
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
        assertTrue(user.isVerified());
        verify(emailVerificationService, never()).createAndSendVerificationEmail(any());
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

    @Test
    void deletedAccountsCannotLogIn() {
        User user = User.builder().username("deleted-account").passwordHash(encoder.encode("Password123!"))
                .role(Role.STUDENT).verified(true).accountStatus(AccountStatus.DELETED).build();
        when(users.findByUsername("deleted-account")).thenReturn(Optional.of(user));
        LoginRequest login = new LoginRequest();
        login.setUsername("deleted-account");
        login.setPassword("Password123!");

        ApiException e = assertThrows(ApiException.class, () -> service.login(login));

        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertTrue(e.getMessage().contains("account has been deleted"));
        verify(accountStatusService, never()).resolveStatus(any());
    }

    @Test
    void unverifiedStudentsCannotLogInWithTheCorrectPassword() {
        User user = User.builder().username("thabo_s").passwordHash(encoder.encode("Password123!"))
                .role(Role.STUDENT).verified(false).build();
        when(users.findByUsername("thabo_s")).thenReturn(Optional.of(user));
        LoginRequest login = new LoginRequest();
        login.setUsername("thabo_s");
        login.setPassword("Password123!");

        ApiException e = assertThrows(ApiException.class, () -> service.login(login));

        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertTrue(e.getMessage().contains("Verify your CPUT email"));
        verify(accountStatusService, never()).resolveStatus(any());
    }

    @Test
    void verifiedStudentsCanLogIn() {
        User user = User.builder().username("thabo_s").passwordHash(encoder.encode("Password123!"))
                .role(Role.STUDENT).verified(true).build();
        when(users.findByUsername("thabo_s")).thenReturn(Optional.of(user));
        LoginRequest login = new LoginRequest();
        login.setUsername("thabo_s");
        login.setPassword("Password123!");

        assertNotNull(service.login(login).getToken());
    }
}
