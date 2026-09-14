package za.ac.cput.cputrade.notification;

import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.notification.dto.NotificationResponse;
import za.ac.cput.cputrade.push.WebPushService;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final WebPushService webPushService;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository,
                                WebPushService webPushService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.webPushService = webPushService;
    }

    /**
     * Called by other services (vendor approval, listing removal, new chat
     * message, wishlist price-drop/sold) — not exposed via a controller.
     *
     * <p>Every notification also fires a real Web Push (if the recipient has
     * any subscribed devices) from this single spot, so every current and
     * future notification type gets push "for free" without every call site
     * having to remember to send one.
     */
    @Transactional
    public void create(User recipient, NotificationType type, String message, String link) {
        notificationRepository.save(Notification.builder()
                .recipient(recipient)
                .type(type)
                .message(message)
                .link(link)
                .build());
        try {
            webPushService.notifyUser(recipient, pushTitle(type), message, link);
        } catch (Exception e) {
            // Push is a bonus channel on top of the in-app notification above, which has
            // already been saved — a push failure must never roll back or hide that.
        }
    }

    private String pushTitle(NotificationType type) {
        return switch (type) {
            case VENDOR_APPROVED -> "You're approved to sell! 🎉";
            case LISTING_REMOVED -> "Listing removed";
            case NEW_MESSAGE -> "New message on CPUTrade";
            case PRICE_DROP -> "Price drop on your wishlist 💸";
            case WISHLIST_ITEM_SOLD -> "Wishlist update";
            case SAVED_SEARCH_MATCH -> "A new listing matches your search 🔎";
        };
    }

    public List<NotificationResponse> myNotifications(Authentication auth) {
        User user = currentUser(auth);
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public Map<String, Long> unreadCount(Authentication auth) {
        User user = currentUser(auth);
        return Map.of("unreadCount", notificationRepository.countByRecipientIdAndReadFalse(user.getId()));
    }

    @Transactional
    public void markRead(Long id, Authentication auth) {
        User user = currentUser(auth);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Notification not found"));
        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw ApiException.forbidden("This notification isn't yours");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead(Authentication auth) {
        User user = currentUser(auth);
        List<Notification> unread = notificationRepository.findByRecipientIdAndReadFalse(user.getId());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> ApiException.unauthorized("Unknown user"));
    }
}
