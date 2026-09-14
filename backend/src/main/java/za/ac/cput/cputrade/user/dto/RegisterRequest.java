package za.ac.cput.cputrade.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Registration payload. {@code role} must be "STUDENT" or "VENDOR" — there is
 * no client-facing way to register as ADMIN.
 */
@Getter
@Setter
public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username must be 3-20 alphanumeric characters or underscores")
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank
    @Pattern(regexp = "STUDENT|VENDOR", message = "role must be STUDENT or VENDOR")
    private String role;
}
