package za.ac.cput.cputrade.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerificationRequest {

    @NotBlank
    @Size(max = 128)
    private String token;
}
