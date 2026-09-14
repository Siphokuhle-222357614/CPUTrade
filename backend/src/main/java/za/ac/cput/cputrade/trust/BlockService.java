package za.ac.cput.cputrade.trust;

import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;
import za.ac.cput.cputrade.user.dto.UserSummaryDto;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BlockService {

    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;

    public BlockService(UserBlockRepository userBlockRepository, UserRepository userRepository) {
        this.userBlockRepository = userBlockRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void block(Long userToBlockId, Authentication auth) {
        User blocker = currentUser(auth);
        if (blocker.getId().equals(userToBlockId)) {
            throw ApiException.badRequest("You cannot block yourself");
        }
        if (userBlockRepository.existsByBlockerIdAndBlockedId(blocker.getId(), userToBlockId)) {
            return; // idempotent — already blocked
        }
        User blocked = userRepository.findById(userToBlockId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        userBlockRepository.save(UserBlock.builder().blocker(blocker).blocked(blocked).build());
    }

    @Transactional
    public void unblock(Long userToUnblockId, Authentication auth) {
        User blocker = currentUser(auth);
        userBlockRepository.findByBlockerIdAndBlockedId(blocker.getId(), userToUnblockId)
                .ifPresent(userBlockRepository::delete);
    }

    public List<UserSummaryDto> myBlockedUsers(Authentication auth) {
        User blocker = currentUser(auth);
        return userBlockRepository.findByBlockerIdOrderByCreatedAtDesc(blocker.getId()).stream()
                .map(b -> UserSummaryDto.from(b.getBlocked()))
                .toList();
    }

    /** True if {@code blockerId} has blocked {@code blockedId} (one direction only). */
    public boolean hasBlocked(Long blockerId, Long blockedId) {
        return userBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> ApiException.unauthorized("Unknown user"));
    }
}
