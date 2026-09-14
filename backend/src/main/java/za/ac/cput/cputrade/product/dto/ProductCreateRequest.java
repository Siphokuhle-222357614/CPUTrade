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

@Getter
@Setter
public class ProductCreateRequest {

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

    /** Base64-encoded JPEG/PNG, decoded size capped at 500KB (enforced in ProductService). */
    private String imageBase64;
}
