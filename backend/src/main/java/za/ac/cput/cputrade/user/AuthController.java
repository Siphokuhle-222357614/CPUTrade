package za.ac.cput.cputrade.user;

import jakarta.validation.Valid;
import za.ac.cput.cputrade.user.dto.AuthResponse;
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

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserSummaryDto> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
