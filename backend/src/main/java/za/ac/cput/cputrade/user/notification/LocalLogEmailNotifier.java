package za.ac.cput.cputrade.user.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import za.ac.cput.cputrade.product.Product;
import za.ac.cput.cputrade.user.User;

/**
 * Local-development mail adapter. Verification links are written to the
 * backend log so the flow can be completed without an SMTP account.
 */
@Slf4j
@Component
@Profile("local")
public class LocalLogEmailNotifier implements EmailNotifier {

    @Override
    public void sendVerificationEmail(User user, String verificationUrl) {
        log.info("[local email] Verification link for {} ({}): {}", user.getEmail(), user.getUsername(), verificationUrl);
    }

    @Override
    public void sendVendorApprovalEmail(User user) {
        log.info("[local email] Vendor approval email would be sent to {} ({})", user.getEmail(), user.getUsername());
    }

    @Override
    public void sendListingRemovedEmail(User seller, Product product) {
        log.info("[local email] Listing-removed email would be sent to {} about product #{} ({})",
                seller.getEmail(), product.getId(), product.getTitle());
    }

    @Override
    public void sendAccountSuspendedEmail(User user, String reason) {
        log.info("[local email] Account-suspended email would be sent to {} ({}). Reason: {}",
                user.getEmail(), user.getUsername(), reason);
    }

    @Override
    public void sendAccountReactivatedEmail(User user) {
        log.info("[local email] Account-reactivated email would be sent to {} ({})", user.getEmail(), user.getUsername());
    }
}
