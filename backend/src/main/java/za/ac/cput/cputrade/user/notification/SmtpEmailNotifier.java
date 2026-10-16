package za.ac.cput.cputrade.user.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import za.ac.cput.cputrade.product.Product;
import za.ac.cput.cputrade.user.User;

@Component
@Profile("smtp")
public class SmtpEmailNotifier implements EmailNotifier {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SmtpEmailNotifier(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendVerificationEmail(User user, String verificationUrl) {
        send(user, "Verify your CPUTrade email",
                "Hello " + user.getUsername() + ",\n\n"
                        + "Verify your email address by opening this link:\n"
                        + verificationUrl + "\n\n"
                        + "This link expires after a limited time. If you did not create a CPUTrade account, ignore this email.");
    }

    @Override
    public void sendVendorApprovalEmail(User user) {
        send(user, "Your CPUTrade vendor account is approved",
                "Hello " + user.getUsername() + ",\n\nYour vendor account has been approved.");
    }

    @Override
    public void sendListingRemovedEmail(User seller, Product product) {
        send(seller, "A CPUTrade listing was removed",
                "Your listing \"" + product.getTitle() + "\" was removed by an administrator.");
    }

    @Override
    public void sendAccountSuspendedEmail(User user, String reason) {
        send(user, "Your CPUTrade account was suspended",
                "Your account was suspended. Reason: " + reason);
    }

    @Override
    public void sendAccountReactivatedEmail(User user) {
        send(user, "Your CPUTrade account was reactivated",
                "Your account has been reactivated. You can sign in again.");
    }

    private void send(User user, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
