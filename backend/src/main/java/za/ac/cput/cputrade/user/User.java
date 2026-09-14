package za.ac.cput.cputrade.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A registered CPUTrade user (student, vendor, or admin).
 *
 * <p>{@code passwordHash} is always a bcrypt hash — the plaintext password is
 * never stored, returned, or logged (US1.2). This entity must never be
 * returned directly from a controller; always map it to a DTO first so the
 * hash can't leak into an API response.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /** Display label, e.g. "{username}@cputmarket.ac.za" (US1.1). */
    @Column(name = "campus_handle", nullable = false, unique = true, length = 150)
    private String campusHandle;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /**
     * US1.3 (stubbed): set to {@code true} immediately on registration since
     * no real email is sent yet. A future real EmailJS/SMTP integration would
     * set this to {@code false} until the user clicks a verification link.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;

    /**
     * US1.5: {@code true} by default for STUDENT/ADMIN; a VENDOR starts
     * {@code false} and cannot create listings until an admin approves them.
     */
    @Column(name = "vendor_approved", nullable = false)
    @Builder.Default
    private boolean vendorApproved = false;

    /**
     * Admin can suspend an account (typically escalating from a report) —
     * a suspended user can't log in at all, and any still-valid JWT they're
     * already holding is rejected on the next request too (see
     * {@code AccountStatusFilter}), so suspension takes effect immediately
     * rather than waiting out the token's remaining expiry.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    /** Why the account was suspended — shown to the user so they know what to appeal. */
    @Column(name = "suspension_reason", length = 500)
    private String suspensionReason;

    /**
     * Optional: if set, the suspension lifts on its own the next time this
     * user logs in after this moment — no appeal needed. Null means the
     * suspension is indefinite until an admin reactivates it or an appeal
     * is approved.
     */
    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Updated (throttled) on every authenticated request by
     * {@code AccountStatusFilter}, which already loads this row per
     * request — "online" is just this being recent (see
     * {@code UserProfileService#isOnline}), not a real presence channel.
     */
    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
