package za.ac.cput.cputrade.trust;

import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.product.Product;
import za.ac.cput.cputrade.product.ProductRepository;
import za.ac.cput.cputrade.trust.dto.ReportRequest;
import za.ac.cput.cputrade.trust.dto.ReportResponse;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.cputrade.admin.AdminActionHistoryService;
import za.ac.cput.cputrade.admin.AdminActionType;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AdminActionHistoryService actionHistory;

    public ReportService(ReportRepository reportRepository, ProductRepository productRepository,
                          UserRepository userRepository, AdminActionHistoryService actionHistory) {
        this.reportRepository = reportRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.actionHistory = actionHistory;
    }

    @Transactional
    public ReportResponse create(ReportRequest request, Authentication auth) {
        User reporter = currentUser(auth);
        Report.ReportBuilder report = Report.builder()
                .reporter(reporter)
                .reason(request.getReason())
                .details(request.getDetails());

        if (request.getTargetType() == ReportTargetType.PRODUCT) {
            Product product = productRepository.findById(request.getTargetId())
                    .orElseThrow(() -> ApiException.notFound("Listing not found"));
            if (product.getSeller().getId().equals(reporter.getId())) {
                throw ApiException.badRequest("You cannot report your own listing");
            }
            report.reportedProduct(product).reportedUser(product.getSeller());
        } else {
            User reportedUser = userRepository.findById(request.getTargetId())
                    .orElseThrow(() -> ApiException.notFound("User not found"));
            if (reportedUser.getId().equals(reporter.getId())) {
                throw ApiException.badRequest("You cannot report yourself");
            }
            report.reportedUser(reportedUser);
        }

        return ReportResponse.from(reportRepository.save(report.build()));
    }

    /** Admin review queue — optionally filtered by status (defaults to everything, newest first). */
    public List<ReportResponse> list(ReportStatus status) {
        List<Report> reports = status != null
                ? reportRepository.findByStatusOrderByCreatedAtDesc(status)
                : reportRepository.findAllByOrderByCreatedAtDesc();
        return reports.stream().map(ReportResponse::from).toList();
    }

    @Transactional
    public ReportResponse setStatus(Long id, ReportStatus status, Authentication actor) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Report not found"));
        AdminActionType action = switch (status) {
            case REVIEWED -> AdminActionType.REPORT_REVIEWED;
            case DISMISSED -> AdminActionType.REPORT_DISMISSED;
            case PENDING -> throw ApiException.badRequest("A report cannot be moved back to pending");
        };
        report.setStatus(status);
        report.setReviewedAt(LocalDateTime.now());
        Report saved = reportRepository.save(report);
        String targetLabel = saved.getReportedProduct() != null
                ? saved.getReportedProduct().getTitle()
                : saved.getReportedUser().getUsername();
        actionHistory.record(actor, action, "REPORT", saved.getId(), targetLabel,
                "Report " + status.name().toLowerCase() + " (" + saved.getReason().name() + ").");
        return ReportResponse.from(saved);
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> ApiException.unauthorized("Unknown user"));
    }
}
