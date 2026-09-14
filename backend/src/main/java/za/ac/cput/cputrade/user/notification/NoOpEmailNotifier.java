package za.ac.cput.cputrade.user.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import za.ac.cput.cputrade.product.Product;
import za.ac.cput.cputrade.user.User;

/**
 * Stub {@link EmailNotifier} for this build pass — logs instead of sending.
 * Swap this out (or add a real implementation and make it {@code @Primary})
 * when EmailJS/SMTP credentials are available; nothing else in the codebase
 * needs to change.
 */
@Slf4j
@Component
public class NoOpEmailNotifier implements EmailNotifier {

    @Override
    public void sendVerificationEmail(User user) {
        log.info("[stub email] Verification email would be sent to {} ({})", user.getEmail(), user.getUsername());
    }

    @Override
    public void sendVendorApprovalEmail(User user) {
        log.info("[stub email] Vendor approval email would be sent to {} ({})", user.getEmail(), user.getUsername());
    }

    @Override
    public void sendListingRemovedEmail(User seller, Product product) {
        log.info("[stub email] Listing-removed email would be sent to {} about product #{} ({})",
                seller.getEmail(), product.getId(), product.getTitle());
    }
}
