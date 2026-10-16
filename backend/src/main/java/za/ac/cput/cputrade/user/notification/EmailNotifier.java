package za.ac.cput.cputrade.user.notification;

import za.ac.cput.cputrade.product.Product;
import za.ac.cput.cputrade.user.User;

/**
 * Outbound email notifications (US1.3, US1.5, US6.3).
 *
 * <p>The local profile logs verification links; the SMTP profile sends mail
 * through a configured SMTP server.
 */
public interface EmailNotifier {

    void sendVerificationEmail(User user, String verificationUrl);

    void sendVendorApprovalEmail(User user);

    void sendListingRemovedEmail(User seller, Product product);

    void sendAccountSuspendedEmail(User user, String reason);

    void sendAccountReactivatedEmail(User user);
}
