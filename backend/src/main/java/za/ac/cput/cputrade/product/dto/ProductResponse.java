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
import java.util.List;

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
    /** How many identical units the seller has (informational — no cart/order flow decrements it). */
    private int quantity;
    /** The first photo, if any — kept for callers that only ever show one cover image (e.g. the marketplace grid). */
    private String imageUrl;
    /** Every photo attached to this listing, in upload order. Empty (never null) when there are none. */
    private List<String> imageUrls;
    private boolean active;
    /** Seller-declared "no longer for sale" — see {@code Product.sold}. */
    private boolean sold;
    private LocalDateTime soldAt;
    /** Null unless the seller named who they sold it to. */
    private String soldToUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** Null when the seller has no ratings yet (US6.1). */
    private Double sellerRatingAverage;
    private long sellerRatingCount;
    /** True once the seller has completed enough sales — see {@code SellerTrust}. */
    private boolean sellerVerified;
    /** US2.4: how many times this listing's detail page has been opened. */
    private long viewCount;
    /** US2.3: true when price is exactly 0 — shown as a "Freecycle" badge. */
    private boolean freecycle;
    /** How many students currently have this listing on their wishlist — a live social-proof signal. */
    private long watcherCount;

    public static ProductResponse from(Product product, RatingSummary sellerRating, long sellerCompletedSales, long watcherCount) {
        List<String> images = product.getImageUrls() != null ? product.getImageUrls() : List.of();
        return ProductResponse.builder()
                .id(product.getId())
                .sellerId(product.getSeller().getId())
                .sellerUsername(product.getSeller().getUsername())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .condition(product.getCondition())
                .quantity(product.getQuantity())
                .imageUrl(images.isEmpty() ? null : images.get(0))
                .imageUrls(images)
                .active(product.isActive())
                .sold(product.isSold())
                .soldAt(product.getSoldAt())
                .soldToUsername(product.getSoldTo() != null ? product.getSoldTo().getUsername() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .sellerRatingAverage(sellerRating.average())
                .sellerRatingCount(sellerRating.count())
                .sellerVerified(za.ac.cput.cputrade.trust.SellerTrust.isVerified(sellerCompletedSales))
                .viewCount(product.getViewCount())
                .freecycle(product.getPrice() != null && product.getPrice().signum() == 0)
                .watcherCount(watcherCount)
                .build();
    }
}
