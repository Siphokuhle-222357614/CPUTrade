package za.ac.cput.cputrade.admin;

import jakarta.validation.Valid;
import za.ac.cput.cputrade.product.dto.ProductResponse;
import za.ac.cput.cputrade.user.dto.SuspendUserRequest;
import za.ac.cput.cputrade.user.dto.UserSummaryDto;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin-only endpoints — gated by {@code hasRole("ADMIN")} in SecurityConfig. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AdminActionHistoryService actionHistory;

    public AdminController(AdminService adminService, AdminActionHistoryService actionHistory) {
        this.adminService = adminService;
        this.actionHistory = actionHistory;
    }

    @GetMapping("/vendor-requests")
    public ResponseEntity<List<UserSummaryDto>> pendingVendorRequests() {
        return ResponseEntity.ok(adminService.pendingVendorRequests());
    }

    @PostMapping("/vendor-requests/{userId}/approve")
    public ResponseEntity<UserSummaryDto> approveVendor(@PathVariable Long userId, Authentication actor) {
        return ResponseEntity.ok(adminService.approveVendor(userId, actor));
    }

    @GetMapping("/listings")
    public ResponseEntity<List<ProductResponse>> allListings() {
        return ResponseEntity.ok(adminService.allListings());
    }

    @PatchMapping("/listings/{id}/deactivate")
    public ResponseEntity<ProductResponse> deactivateListing(@PathVariable Long id, Authentication actor) {
        return ResponseEntity.ok(adminService.deactivateListing(id, actor));
    }

    @PostMapping("/users/{userId}/suspend")
    public ResponseEntity<UserSummaryDto> suspendUser(@PathVariable Long userId, @Valid @RequestBody SuspendUserRequest request,
                                                       Authentication actor) {
        return ResponseEntity.ok(adminService.suspendUser(userId, request, actor));
    }

    @PostMapping("/users/{userId}/reactivate")
    public ResponseEntity<UserSummaryDto> reactivateUser(@PathVariable Long userId, Authentication actor) {
        return ResponseEntity.ok(adminService.reactivateUser(userId, actor));
    }

    @GetMapping("/users/suspended")
    public ResponseEntity<List<UserSummaryDto>> suspendedUsers() {
        return ResponseEntity.ok(adminService.suspendedUsers());
    }

    /** Every student and vendor — the admin's general user-management list (US6.3-adjacent). */
    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryDto>> allUsers() {
        return ResponseEntity.ok(adminService.allNonAdminUsers());
    }

    @GetMapping("/history")
    public ResponseEntity<List<AdminActionResponse>> actionHistory() {
        return ResponseEntity.ok(actionHistory.latest());
    }
}
