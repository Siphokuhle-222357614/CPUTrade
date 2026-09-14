package za.ac.cput.cputrade.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /** Admin's pending vendor-request queue (US1.5) — no separate table needed. */
    List<User> findByRoleAndVendorApprovedFalse(Role role);
}
