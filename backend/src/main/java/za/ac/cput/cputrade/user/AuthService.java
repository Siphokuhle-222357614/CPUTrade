package za.ac.cput.cputrade.user;

import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.security.JwtUtil;
import za.ac.cput.cputrade.user.dto.AuthResponse;
import za.ac.cput.cputrade.user.dto.LoginRequest;
import za.ac.cput.cputrade.user.dto.RegisterRequest;
import za.ac.cput.cputrade.user.dto.UserSummaryDto;
import za.ac.cput.cputrade.user.notification.EmailNotifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailNotifier emailNotifier;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil, EmailNotifier emailNotifier) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailNotifier = emailNotifier;
    }

    @Transactional
    public UserSummaryDto register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw ApiException.conflict("Username already taken — try another");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("An account with this email already exists");
        }

        Role role = Role.valueOf(request.getRole());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .campusHandle(request.getUsername() + "@cputmarket.ac.za")
                .passwordHash(passwordEncoder.encode(request.getPassword())) // bcrypt (US1.2)
                .role(role)
                .verified(true) // US1.3 stub: auto-verify, no real email sent yet
                .vendorApproved(role != Role.VENDOR) // STUDENT/ADMIN pre-approved; VENDOR needs admin approval (US1.5)
                .build();

        User saved = userRepository.save(user);
        emailNotifier.sendVerificationEmail(saved);

        return UserSummaryDto.from(saved);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> ApiException.unauthorized("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationSeconds())
                .user(UserSummaryDto.from(user))
                .build();
    }
}
