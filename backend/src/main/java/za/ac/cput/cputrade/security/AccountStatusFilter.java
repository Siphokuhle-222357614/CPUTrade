package za.ac.cput.cputrade.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;
import za.ac.cput.cputrade.user.AccountStatus;
import za.ac.cput.cputrade.user.AccountStatusService;
import za.ac.cput.cputrade.user.Role;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Two account-level rules that apply on <em>every</em> authenticated
 * request, not just at login:
 *
 * <ol>
 *   <li>A suspended account is rejected outright, even mid-session — this
 *       is what makes suspension take effect immediately instead of only
 *       once the user's current JWT happens to expire (up to 24h later).</li>
 *   <li>A VENDOR who isn't yet admin-approved (US1.5) can browse (GET) but
 *       can't perform any write action anywhere — not just "can't create
 *       listings." A short allowlist covers self-service actions (reading
 *       notifications, blocking a harasser, appealing) that aren't "running
 *       a business" and so don't need to wait on approval.</li>
 * </ol>
 *
 * <p>Runs after {@link JwtAuthFilter} so the SecurityContext is already
 * populated for this request when it checks.
 */
@Component
public class AccountStatusFilter extends OncePerRequestFilter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    private static final List<RequestMatcher> VENDOR_ALLOWLIST_WHILE_PENDING = List.of(
            PathPatternRequestMatcher.pathPattern(HttpMethod.PATCH, "/api/notifications/**"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/users/*/block"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.DELETE, "/api/users/*/block"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/appeals")
    );

    private final UserRepository userRepository;
    private final AccountStatusService accountStatusService;
    private final ObjectMapper objectMapper;

    public AccountStatusFilter(UserRepository userRepository, AccountStatusService accountStatusService,
                                ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.accountStatusService = accountStatusService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            Optional<User> maybeUser = userRepository.findByUsername(auth.getName());
            if (maybeUser.isPresent()) {
                User user = accountStatusService.resolveStatus(maybeUser.get());

                if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
                    String reasonSuffix = user.getSuspensionReason() != null ? ": " + user.getSuspensionReason() + "." : ".";
                    writeError(response, "Your account has been suspended" + reasonSuffix
                            + " If you believe this is a mistake, submit an appeal.");
                    return;
                }

                boolean mutating = !SAFE_METHODS.contains(request.getMethod());
                boolean pendingVendor = user.getRole() == Role.VENDOR && !user.isVendorApproved();
                if (mutating && pendingVendor && VENDOR_ALLOWLIST_WHILE_PENDING.stream().noneMatch(m -> m.matches(request))) {
                    writeError(response,
                            "Your vendor account is pending admin approval — you can browse, but can't make changes yet.");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", 403,
                "error", "Forbidden",
                "message", message
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
