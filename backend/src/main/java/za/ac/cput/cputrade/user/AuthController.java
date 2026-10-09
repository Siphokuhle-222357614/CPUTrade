package za.ac.cput.cputrade.user;

import jakarta.validation.Valid;
import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.security.LoginRateLimiter;
import za.ac.cput.cputrade.user.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import za.ac.cput.cputrade.user.dto.LoginRequest;
import za.ac.cput.cputrade.user.dto.RegisterRequest;
import za.ac.cput.cputrade.user.dto.UserSummaryDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiter loginRateLimiter;

    public AuthController(AuthService authService, LoginRateLimiter loginRateLimiter) {
        this.authService = authService;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/register")
    public ResponseEntity<UserSummaryDto> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        // Keyed by client address AND username, so an attacker hammering one account
        // from their own address can't lock the real student out from theirs.
        String key = http.getRemoteAddr() + "|" + request.getUsername().toLowerCase();
        loginRateLimiter.checkAllowed(key);
        try {
            AuthResponse response = authService.login(request);
            loginRateLimiter.recordSuccess(key);
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            // Only wrong credentials count; a suspended account (403) is not a guessing attempt.
            if (e.getStatus() == HttpStatus.UNAUTHORIZED) {
                loginRateLimiter.recordFailure(key);
            }
            throw e;
        }
    }
}
