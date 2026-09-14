package za.ac.cput.cputrade.trust;

import jakarta.validation.Valid;
import za.ac.cput.cputrade.trust.dto.ReportRequest;
import za.ac.cput.cputrade.trust.dto.ReportResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * US-trust: report a listing/user (any authenticated user), reviewed by an
 * admin. The admin endpoints live under {@code /api/admin/**} so they're
 * already covered by SecurityConfig's {@code hasRole("ADMIN")} rule with no
 * extra security config needed.
 */
@RestController
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/api/reports")
    public ResponseEntity<ReportResponse> create(@Valid @RequestBody ReportRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.create(request, auth));
    }

    @GetMapping("/api/admin/reports")
    public ResponseEntity<List<ReportResponse>> list(@RequestParam(required = false) ReportStatus status) {
        return ResponseEntity.ok(reportService.list(status));
    }

    @PatchMapping("/api/admin/reports/{id}/review")
    public ResponseEntity<ReportResponse> markReviewed(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.setStatus(id, ReportStatus.REVIEWED));
    }

    @PatchMapping("/api/admin/reports/{id}/dismiss")
    public ResponseEntity<ReportResponse> dismiss(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.setStatus(id, ReportStatus.DISMISSED));
    }
}
