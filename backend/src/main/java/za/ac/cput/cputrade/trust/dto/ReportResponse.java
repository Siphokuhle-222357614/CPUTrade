package za.ac.cput.cputrade.trust.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import za.ac.cput.cputrade.trust.Report;
import za.ac.cput.cputrade.trust.ReportReason;
import za.ac.cput.cputrade.trust.ReportStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ReportResponse {
    private Long id;
    private String reporterUsername;
    private Long reportedUserId;
    private String reportedUsername;
    private Long reportedProductId;
    private String reportedProductTitle;
    private ReportReason reason;
    private String details;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    public static ReportResponse from(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reporterUsername(report.getReporter().getUsername())
                .reportedUserId(report.getReportedUser() != null ? report.getReportedUser().getId() : null)
                .reportedUsername(report.getReportedUser() != null ? report.getReportedUser().getUsername() : null)
                .reportedProductId(report.getReportedProduct() != null ? report.getReportedProduct().getId() : null)
                .reportedProductTitle(report.getReportedProduct() != null ? report.getReportedProduct().getTitle() : null)
                .reason(report.getReason())
                .details(report.getDetails())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .reviewedAt(report.getReviewedAt())
                .build();
    }
}
