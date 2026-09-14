package za.ac.cput.cputrade.trust.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import za.ac.cput.cputrade.trust.ReportReason;
import za.ac.cput.cputrade.trust.ReportTargetType;

@Getter
@Setter
public class ReportRequest {

    @NotNull
    private ReportTargetType targetType;

    /** The product id (targetType=PRODUCT) or user id (targetType=USER) being reported. */
    @NotNull
    private Long targetId;

    @NotNull
    private ReportReason reason;

    @Size(max = 1000)
    private String details;
}
