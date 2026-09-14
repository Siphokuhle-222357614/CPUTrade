package za.ac.cput.cputrade.profile;

import za.ac.cput.cputrade.profile.dto.PresenceResponse;
import za.ac.cput.cputrade.profile.dto.PublicProfileResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Both endpoints require auth (default rule) — a profile/presence lookup is only useful to someone already signed in. */
@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<PublicProfileResponse> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userProfileService.getProfile(id));
    }

    @GetMapping("/{id}/presence")
    public ResponseEntity<PresenceResponse> getPresence(@PathVariable Long id) {
        return ResponseEntity.ok(userProfileService.getPresence(id));
    }
}
