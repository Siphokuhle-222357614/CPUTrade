package za.ac.cput.cputrade.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import za.ac.cput.cputrade.product.Category;
import za.ac.cput.cputrade.product.SavedSearch;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SavedSearchResponse {
    private Long id;
    private String keyword;
    private Category category;
    private BigDecimal maxPrice;
    private LocalDateTime createdAt;

    public static SavedSearchResponse from(SavedSearch savedSearch) {
        return SavedSearchResponse.builder()
                .id(savedSearch.getId())
                .keyword(savedSearch.getKeyword())
                .category(savedSearch.getCategory())
                .maxPrice(savedSearch.getMaxPrice())
                .createdAt(savedSearch.getCreatedAt())
                .build();
    }
}
