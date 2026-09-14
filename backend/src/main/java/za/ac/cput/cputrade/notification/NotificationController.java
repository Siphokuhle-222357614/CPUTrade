package za.ac.cput.cputrade.notification;

import za.ac.cput.cputrade.notification.dto.NotificationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** US4.3 — in-app notifications. All endpoints require auth (default rule in SecurityConfig). */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> myNotifications(Authentication auth) {
        return ResponseEntity.ok(notificationService.myNotifications(auth));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(Authentication auth) {
        return ResponseEntity.ok(notificationService.unreadCount(auth));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id, Authentication auth) {
        notificationService.markRead(id, auth);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(Authentication auth) {
        notificationService.markAllRead(auth);
        return ResponseEntity.noContent().build();
    }
}
