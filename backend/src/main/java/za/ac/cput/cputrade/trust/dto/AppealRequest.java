package za.ac.cput.cputrade.trust.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Submitted without auth — a suspended user has no valid way to log in and get a token. */
@Getter
@Setter
public class AppealRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Size(max = 2000)
    private String message;
}
