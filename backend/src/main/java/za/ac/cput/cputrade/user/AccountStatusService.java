package za.ac.cput.cputrade.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Shared suspension logic used by both {@code AuthService} (at login) and
 * {@code AccountStatusFilter} (on every subsequent request, so an
 * already-issued JWT is rejected immediately on suspension rather than
 * staying valid until it naturally expires).
 */
@Service
public class AccountStatusService {

    private final UserRepository userRepository;

    public AccountStatusService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * If {@code user} has a timed suspension whose time has passed, lifts it
     * and persists the change. Returns the (possibly now-updated) user —
     * always check {@code accountStatus} on the *returned* value.
     */
    @Transactional
    public User resolveStatus(User user) {
        boolean expired = user.getAccountStatus() == AccountStatus.SUSPENDED
                && user.getSuspendedUntil() != null
                && user.getSuspendedUntil().isBefore(LocalDateTime.now());
        if (!expired) {
            return user;
        }
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setSuspendedUntil(null);
        user.setSuspensionReason(null);
        return userRepository.save(user);
    }
}
