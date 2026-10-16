package za.ac.cput.cputrade.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Plain SELECT ... FOR UPDATE avoids Hibernate's FOR UPDATE OF alias syntax,
     * which MariaDB rejects, while retaining the row lock used to throttle resends.
     */
    @Query(value = "SELECT * FROM users WHERE LOWER(email) = LOWER(:email) FOR UPDATE", nativeQuery = true)
    Optional<User> findByEmailIgnoreCaseForVerificationUpdate(@Param("email") String email);

    Optional<User> findByEmailVerificationTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update User u
            set u.verified = true,
                u.emailVerificationTokenHash = null,
                u.emailVerificationExpiresAt = null
            where u.emailVerificationTokenHash = :tokenHash
              and u.emailVerificationExpiresAt > :now
              and u.verified = false
            """)
    int consumeEmailVerificationToken(@Param("tokenHash") String tokenHash, @Param("now") LocalDateTime now);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /** Admin's pending vendor-request queue (US1.5) — no separate table needed. */
    List<User> findByRoleAndVendorApprovedFalse(Role role);

    List<User> findByRoleAndVendorApprovedFalseAndAccountStatusNot(Role role, AccountStatus accountStatus);

    List<User> findByAccountStatusOrderByCreatedAtDesc(AccountStatus accountStatus);

    /** Admin's user-management list — every student and vendor (never admins), for suspending without needing a report first. */
    List<User> findByRoleNotOrderByCreatedAtDesc(Role role);

    List<User> findByRoleNotAndAccountStatusNotOrderByCreatedAtDesc(Role role, AccountStatus accountStatus);
}
