package za.ac.cput.cputrade.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import za.ac.cput.cputrade.product.Category;
import za.ac.cput.cputrade.product.Condition;

import java.math.BigDecimal;

/** Full replace of a listing's editable fields (US2.2) — not a partial patch. */
@Getter
@Setter
public class ProductUpdateRequest {

    @NotBlank
    @Size(max = 150)
    private String title;

    @Size(max = 5000)
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", message = "price must be >= 0")
    private BigDecimal price;

    @NotNull
    private Category category;

    @NotNull
    private Condition condition;

    /** Optional: omit to keep the existing image. */
    private String imageBase64;
}
