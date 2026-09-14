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

import java.util.regex.Pattern;

@Service
public class AuthService {

    // Students self-register with no other verification step, so this is
    // the trust boundary that makes the platform "CPUT-only" rather than
    // open to anyone. VENDOR accounts are deliberately exempt — they go
    // through admin approval instead (US1.5), which is the appropriate
    // check for a legitimate outside business, not a university email.
    private static final Pattern CPUT_STUDENT_EMAIL =
            Pattern.compile("^[^@\\s]+@(mycput|cput)\\.ac\\.za$", Pattern.CASE_INSENSITIVE);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailNotifier emailNotifier;
    private final AccountStatusService accountStatusService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil, EmailNotifier emailNotifier, AccountStatusService accountStatusService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailNotifier = emailNotifier;
        this.accountStatusService = accountStatusService;
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

        if (role == Role.STUDENT && !CPUT_STUDENT_EMAIL.matcher(request.getEmail()).matches()) {
            throw ApiException.badRequest(
                    "Students must register with an official CPUT email address (@mycput.ac.za or @cput.ac.za)");
        }

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

        user = accountStatusService.resolveStatus(user); // lifts an expired timed suspension first
        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            String reasonSuffix = user.getSuspensionReason() != null ? ": " + user.getSuspensionReason() + "." : ".";
            throw ApiException.forbidden(
                    "Your account has been suspended" + reasonSuffix
                            + " If you believe this is a mistake, submit an appeal.");
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
