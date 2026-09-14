package za.ac.cput.cputrade.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import za.ac.cput.cputrade.product.Category;
import za.ac.cput.cputrade.product.Condition;
import za.ac.cput.cputrade.product.Product;

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

    public static ProductResponse from(Product product) {
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
                .build();
    }
}
