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

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ReportService(ReportRepository reportRepository, ProductRepository productRepository,
                          UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
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
    public ReportResponse setStatus(Long id, ReportStatus status) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Report not found"));
        report.setStatus(status);
        report.setReviewedAt(LocalDateTime.now());
        return ReportResponse.from(reportRepository.save(report));
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> ApiException.unauthorized("Unknown user"));
    }
}
