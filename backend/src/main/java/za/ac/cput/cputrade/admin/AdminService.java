package za.ac.cput.cputrade.admin;

import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.product.Product;
import za.ac.cput.cputrade.product.ProductRepository;
import za.ac.cput.cputrade.product.dto.ProductResponse;
import za.ac.cput.cputrade.user.Role;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;
import za.ac.cput.cputrade.user.dto.UserSummaryDto;
import za.ac.cput.cputrade.user.notification.EmailNotifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final EmailNotifier emailNotifier;

    public AdminService(UserRepository userRepository, ProductRepository productRepository, EmailNotifier emailNotifier) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.emailNotifier = emailNotifier;
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
        return UserSummaryDto.from(saved);
    }

    /** All listings, active and inactive, for moderation (US6.3). */
    public List<ProductResponse> allListings() {
        return productRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional
    public ProductResponse deactivateListing(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ApiException.notFound("Listing not found"));
        product.setActive(false);
        Product saved = productRepository.save(product);
        emailNotifier.sendListingRemovedEmail(product.getSeller(), saved);
        return ProductResponse.from(saved);
    }
}
