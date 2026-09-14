package za.ac.cput.cputrade.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Appends one or more photos to an existing listing (up to the 6-per-listing cap, enforced in the service). */
@Getter
@Setter
public class AddImagesRequest {

    @NotEmpty
    @Size(max = 6, message = "Up to 6 photos are allowed per listing")
    private List<@NotBlank String> images;
}
