package za.ac.cput.cputrade.admin;

import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.notification.NotificationService;
import za.ac.cput.cputrade.notification.NotificationType;
import za.ac.cput.cputrade.product.Product;
import za.ac.cput.cputrade.product.ProductRepository;
import za.ac.cput.cputrade.product.ProductService;
import za.ac.cput.cputrade.product.dto.ProductResponse;
import za.ac.cput.cputrade.user.AccountStatus;
import za.ac.cput.cputrade.user.Role;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;
import za.ac.cput.cputrade.user.dto.SuspendUserRequest;
import za.ac.cput.cputrade.user.dto.UserSummaryDto;
import za.ac.cput.cputrade.user.notification.EmailNotifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final EmailNotifier emailNotifier;
    private final NotificationService notificationService;

    public AdminService(UserRepository userRepository, ProductRepository productRepository,
                         ProductService productService, EmailNotifier emailNotifier,
                         NotificationService notificationService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.emailNotifier = emailNotifier;
        this.notificationService = notificationService;
    }

    /** US1.5: admin's pending vendor-request queue. */
    public List<UserSummaryDto> pendingVendorRequests() {
        return userRepository.findByRoleAndVendorApprovedFalse(Role.VENDOR).stream()
                .map(UserSummaryDto::from)
                .toList();
    }

    @Transactional
    public UserSummaryDto approveVendor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        if (user.getRole() != Role.VENDOR) {
            throw ApiException.badRequest("User is not a vendor");
        }
        user.setVendorApproved(true);
        User saved = userRepository.save(user);
        emailNotifier.sendVendorApprovalEmail(saved);
        notificationService.create(saved, NotificationType.VENDOR_APPROVED,
                "Your vendor account has been approved — you can now list items for sale.", "/products/new");
        return UserSummaryDto.from(saved);
    }

    /** All listings, active and inactive, for moderation (US6.3). */
    public List<ProductResponse> allListings() {
        return productRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(productService::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse deactivateListing(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ApiException.notFound("Listing not found"));
        product.setActive(false);
        Product saved = productRepository.save(product);
        emailNotifier.sendListingRemovedEmail(product.getSeller(), saved);
        // No link: the listing is now inactive, so its detail page 404s for a non-admin viewer.
        notificationService.create(product.getSeller(), NotificationType.LISTING_REMOVED,
                "Your listing \"" + product.getTitle() + "\" was removed by an admin.", null);
        return productService.toResponse(saved);
    }

    /**
     * Suspends a user's account — typically escalating from a report. They
     * can't log in (and any still-valid JWT is rejected on the next request
     * too — see {@code AccountStatusFilter}) until an admin reactivates them
     * directly, an appeal is approved, or an optional {@code suspendedUntil}
     * time passes.
     */
    @Transactional
    public UserSummaryDto suspendUser(Long userId, SuspendUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        if (user.getRole() == Role.ADMIN) {
            throw ApiException.badRequest("Cannot suspend an admin account");
        }
        user.setAccountStatus(AccountStatus.SUSPENDED);
        user.setSuspensionReason(request.getReason());
        user.setSuspendedUntil(request.getSuspendedUntil());
        User saved = userRepository.save(user);
        emailNotifier.sendAccountSuspendedEmail(saved, request.getReason());
        return UserSummaryDto.from(saved);
    }

    @Transactional
    public UserSummaryDto reactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setSuspendedUntil(null);
        user.setSuspensionReason(null);
        User saved = userRepository.save(user);
        emailNotifier.sendAccountReactivatedEmail(saved);
        return UserSummaryDto.from(saved);
    }

    public List<UserSummaryDto> suspendedUsers() {
        return userRepository.findByAccountStatusOrderByCreatedAtDesc(AccountStatus.SUSPENDED).stream()
                .map(UserSummaryDto::from)
                .toList();
    }

    /** Every student and vendor (never admins) — so an admin can suspend someone directly, not only via a filed report. */
    public List<UserSummaryDto> allNonAdminUsers() {
        return userRepository.findByRoleNotOrderByCreatedAtDesc(Role.ADMIN).stream()
                .map(UserSummaryDto::from)
                .toList();
    }
}
