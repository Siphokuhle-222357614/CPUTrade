package za.ac.cput.cputrade.push;

import jakarta.validation.Valid;
import za.ac.cput.cputrade.push.dto.PushSubscriptionRequest;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/push")
public class PushController {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final UserRepository userRepository;
    private final String vapidPublicKey;

    public PushController(PushSubscriptionRepository pushSubscriptionRepository, UserRepository userRepository,
                           @Value("${push.vapid.public-key}") String vapidPublicKey) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.userRepository = userRepository;
        this.vapidPublicKey = vapidPublicKey;
    }

    /** Public — the frontend needs this before it can ever call pushManager.subscribe(). */
    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> publicKey() {
        return ResponseEntity.ok(Map.of("publicKey", vapidPublicKey));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@Valid @RequestBody PushSubscriptionRequest request, Authentication auth) {
        User user = currentUser(auth);
        // A browser occasionally re-subscribes with the same endpoint (e.g. after clearing the
        // permission and re-granting it) — replace rather than reject on that unique constraint.
        pushSubscriptionRepository.findByEndpoint(request.getEndpoint())
                .ifPresent(pushSubscriptionRepository::delete);
        pushSubscriptionRepository.save(PushSubscription.builder()
                .user(user)
                .endpoint(request.getEndpoint())
                .p256dhKey(request.getKeys().getP256dh())
                .authKey(request.getKeys().getAuth())
                .build());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/subscribe")
    public ResponseEntity<Void> unsubscribe(@RequestParam String endpoint, Authentication auth) {
        User user = currentUser(auth);
        pushSubscriptionRepository.deleteByUserIdAndEndpoint(user.getId(), endpoint);
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> za.ac.cput.cputrade.common.exception.ApiException.unauthorized("Unknown user"));
    }
}
