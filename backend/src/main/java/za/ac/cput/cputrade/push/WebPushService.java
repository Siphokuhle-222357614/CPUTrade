package za.ac.cput.cputrade.push;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import za.ac.cput.cputrade.user.User;

import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;

/**
 * Sends real Web Push messages (VAPID) so an installed PWA hears about a new
 * message/price-drop/etc. even while it's closed — the reason the in-app
 * {@code NotificationService} calls this after every notification it saves,
 * rather than each feature (chat, wishlist, admin) calling it separately.
 *
 * <p>Every public method here is deliberately non-fatal: a push provider
 * outage or a stale subscription must never break the notification/business
 * flow that triggered it, the same way {@code EmailNotifier} never does.
 */
@Service
public class WebPushService {

    private static final Logger log = LoggerFactory.getLogger(WebPushService.class);

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final PushService pushService;

    public WebPushService(PushSubscriptionRepository pushSubscriptionRepository,
                           @Value("${push.vapid.public-key}") String publicKey,
                           @Value("${push.vapid.private-key}") String privateKey,
                           @Value("${push.vapid.subject}") String subject) throws GeneralSecurityException {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.pushService = new PushService(publicKey, privateKey, subject);
    }

    public void notifyUser(User recipient, String title, String body, String link) {
        List<PushSubscription> subscriptions = pushSubscriptionRepository.findByUserId(recipient.getId());
        if (subscriptions.isEmpty()) {
            return;
        }

        // Hand-built rather than pulled through a JSON library: the payload is
        // three known flat string fields, and the project's Jackson dependency
        // is runtime-scoped here (pulled in transitively via jjwt-jackson), so
        // it isn't available at compile time for this class.
        String payload = "{\"title\":\"" + jsonEscape(title) + "\",\"body\":\"" + jsonEscape(body)
                + "\",\"link\":\"" + jsonEscape(link != null ? link : "/") + "\"}";

        for (PushSubscription subscription : subscriptions) {
            try {
                Subscription target = new Subscription(subscription.getEndpoint(),
                        new Subscription.Keys(subscription.getP256dhKey(), subscription.getAuthKey()));
                HttpResponse response = pushService.send(new Notification(target, payload));
                int status = response.getStatusLine().getStatusCode();
                if (status == 404 || status == 410) {
                    // The browser/push service dropped this subscription (uninstalled, expired, ...) — stop retrying it.
                    pushSubscriptionRepository.deleteByEndpoint(subscription.getEndpoint());
                }
            } catch (Exception e) {
                log.warn("Push delivery failed for subscription {}", subscription.getId(), e);
            }
        }
    }

    private static String jsonEscape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
