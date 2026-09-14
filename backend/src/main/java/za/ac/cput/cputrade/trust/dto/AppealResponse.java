package za.ac.cput.cputrade.trust.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import za.ac.cput.cputrade.trust.Appeal;
import za.ac.cput.cputrade.trust.AppealStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AppealResponse {
    private Long id;
    private String username;
    private String message;
    private AppealStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public static AppealResponse from(Appeal appeal) {
        return AppealResponse.builder()
                .id(appeal.getId())
                .username(appeal.getUser().getUsername())
                .message(appeal.getMessage())
                .status(appeal.getStatus())
                .createdAt(appeal.getCreatedAt())
                .resolvedAt(appeal.getResolvedAt())
                .build();
    }
}
