package za.ac.cput.cputrade.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SuspendUserRequest {

    @Size(max = 500)
    private String reason;

    /** Optional: if set, the suspension lifts on its own after this moment — otherwise it's indefinite. */
    private LocalDateTime suspendedUntil;
}
