package za.ac.cput.cputrade.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import za.ac.cput.cputrade.product.Category;
import za.ac.cput.cputrade.product.Condition;
import za.ac.cput.cputrade.product.Product;
import za.ac.cput.cputrade.rating.dto.RatingSummary;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private Long sellerId;
    private String sellerUsername;
    private String title;
    private String description;
    private BigDecimal price;
    private Category category;
    private Condition condition;
    private String imageBase64;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** Null when the seller has no ratings yet (US6.1). */
    private Double sellerRatingAverage;
    private long sellerRatingCount;
    /** US2.4: how many times this listing's detail page has been opened. */
    private long viewCount;
    /** US2.3: true when price is exactly 0 — shown as a "Freecycle" badge. */
    private boolean freecycle;

    public static ProductResponse from(Product product, RatingSummary sellerRating) {
        return ProductResponse.builder()
                .id(product.getId())
                .sellerId(product.getSeller().getId())
                .sellerUsername(product.getSeller().getUsername())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .condition(product.getCondition())
                .imageBase64(product.getImageBase64())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .sellerRatingAverage(sellerRating.average())
                .sellerRatingCount(sellerRating.count())
                .viewCount(product.getViewCount())
                .freecycle(product.getPrice() != null && product.getPrice().signum() == 0)
                .build();
    }
}
