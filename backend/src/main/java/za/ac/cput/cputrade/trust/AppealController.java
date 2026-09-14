package za.ac.cput.cputrade.trust;

import jakarta.validation.Valid;
import za.ac.cput.cputrade.trust.dto.AppealRequest;
import za.ac.cput.cputrade.trust.dto.AppealResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * A suspended user can't log in, so {@code POST /api/appeals} is
 * deliberately public (permitted in SecurityConfig) — identified by
 * username instead of a JWT. Admin review lives under {@code /api/admin/**},
 * already covered by the existing {@code hasRole("ADMIN")} rule.
 */
@RestController
public class AppealController {

    private final AppealService appealService;

    public AppealController(AppealService appealService) {
        this.appealService = appealService;
    }

    @PostMapping("/api/appeals")
    public ResponseEntity<AppealResponse> submit(@Valid @RequestBody AppealRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appealService.submit(request));
    }

    @GetMapping("/api/admin/appeals")
    public ResponseEntity<List<AppealResponse>> list(@RequestParam(required = false) AppealStatus status) {
        return ResponseEntity.ok(appealService.list(status));
    }

    @PatchMapping("/api/admin/appeals/{id}/approve")
    public ResponseEntity<AppealResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(appealService.approve(id));
    }

    @PatchMapping("/api/admin/appeals/{id}/reject")
    public ResponseEntity<AppealResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(appealService.reject(id));
    }
}
