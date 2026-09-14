package za.ac.cput.cputrade.user.notification;

import za.ac.cput.cputrade.product.Product;
import za.ac.cput.cputrade.user.User;

/**
 * Outbound email notifications (US1.3, US1.5, US6.3).
 *
 * <p>This is the ONLY place a real email provider (EmailJS, SMTP, etc.)
 * should be wired in later. For this first build pass, the sole
 * implementation is {@link NoOpEmailNotifier}, which just logs — no real
 * email is sent, and every account auto-verifies on registration.
 */
public interface EmailNotifier {

    void sendVerificationEmail(User user);

    void sendVendorApprovalEmail(User user);

    void sendListingRemovedEmail(User seller, Product product);

    void sendAccountSuspendedEmail(User user, String reason);

    void sendAccountReactivatedEmail(User user);
}
