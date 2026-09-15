package za.ac.cput.cputrade.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import za.ac.cput.cputrade.product.Campus;
import za.ac.cput.cputrade.product.Category;
import za.ac.cput.cputrade.product.Condition;

import java.math.BigDecimal;

/**
 * Full replace of a listing's editable text/detail fields (US2.2) — not a
 * partial patch. Photos are deliberately not part of this DTO: they're
 * managed incrementally through the dedicated add/remove photo endpoints
 * instead, since re-sending every existing photo as Base64 on every text
 * edit would be wasteful and easy to get wrong.
 */
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

    @NotNull
    private Campus campus;

    @NotNull
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;
}
