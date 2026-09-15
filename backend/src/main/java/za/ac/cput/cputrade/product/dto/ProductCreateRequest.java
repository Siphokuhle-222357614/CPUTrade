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
import java.util.List;

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

    @NotNull
    private Campus campus;

    /** How many identical units the seller has. Defaults to 1 if omitted. */
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;

    /** A plain boolean, not Boolean — omitting it is the same as false. */
    private boolean openToSwap;

    @Size(max = 300)
    private String swapPreferences;

    /**
     * Base64-encoded JPEG/PNG photos, decoded size capped at 500KB each —
     * this is the upload wire format only, {@code ImageStorageService}
     * decodes and writes each one to disk, and only the resulting URLs are
     * ever persisted or returned. Optional; up to 6 per listing.
     */
    @Size(max = 6, message = "Up to 6 photos are allowed per listing")
    private List<String> images;

    public int getQuantityOrDefault() {
        return quantity != null ? quantity : 1;
    }
}
