package za.ac.cput.cputrade.trust;

import za.ac.cput.cputrade.user.dto.UserSummaryDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Blocking another user silences messaging between the two of you in both directions (see ChatService). */
@RestController
@RequestMapping("/api/users")
public class BlockController {

    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @PostMapping("/{userId}/block")
    public ResponseEntity<Void> block(@PathVariable Long userId, Authentication auth) {
        blockService.block(userId, auth);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/block")
    public ResponseEntity<Void> unblock(@PathVariable Long userId, Authentication auth) {
        blockService.unblock(userId, auth);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/blocked")
    public ResponseEntity<List<UserSummaryDto>> myBlockedUsers(Authentication auth) {
        return ResponseEntity.ok(blockService.myBlockedUsers(auth));
    }
}
