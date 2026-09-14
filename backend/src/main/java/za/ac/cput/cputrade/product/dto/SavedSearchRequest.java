package za.ac.cput.cputrade.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import za.ac.cput.cputrade.product.Category;

import java.math.BigDecimal;

@Getter
@Setter
public class SavedSearchRequest {

    @Size(max = 150)
    private String keyword;

    private Category category;

    @DecimalMin(value = "0.0", message = "maxPrice must be >= 0")
    private BigDecimal maxPrice;
}
