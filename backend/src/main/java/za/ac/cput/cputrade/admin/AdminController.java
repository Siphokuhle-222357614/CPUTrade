package za.ac.cput.cputrade.admin;

import za.ac.cput.cputrade.product.dto.ProductResponse;
import za.ac.cput.cputrade.user.dto.UserSummaryDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin-only endpoints — gated by {@code hasRole("ADMIN")} in SecurityConfig. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/vendor-requests")
    public ResponseEntity<List<UserSummaryDto>> pendingVendorRequests() {
        return ResponseEntity.ok(adminService.pendingVendorRequests());
    }

    @PostMapping("/vendor-requests/{userId}/approve")
    public ResponseEntity<UserSummaryDto> approveVendor(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.approveVendor(userId));
    }

    @GetMapping("/listings")
    public ResponseEntity<List<ProductResponse>> allListings() {
        return ResponseEntity.ok(adminService.allListings());
    }

    @PatchMapping("/listings/{id}/deactivate")
    public ResponseEntity<ProductResponse> deactivateListing(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.deactivateListing(id));
    }
}
