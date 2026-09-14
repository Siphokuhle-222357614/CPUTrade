package za.ac.cput.cputrade.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.ac.cput.cputrade.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A product listing (US2.1, US2.2, US3.1, US6.3).
 *
 * <p>Photos are validated (JPEG/PNG, decoded size &lt;= 500KB — see
 * {@code ImageValidator}) and written to disk by {@code ImageStorageService}
 * before this entity is ever persisted; {@code imageUrls} only ever holds
 * the resulting URLs, never the image bytes themselves.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    // Mapped to "item_condition" — "condition" is a reserved word in MySQL
    // (used in stored-routine syntax) and breaks CREATE TABLE unquoted.
    @Enumerated(EnumType.STRING)
    @Column(name = "item_condition", nullable = false, length = 20)
    private Condition condition;

    /**
     * How many identical units the seller has left (e.g. a vendor with 10 of
     * the same phone case) — decremented by one each time
     * {@code ProductService#markSold} is called, rather than always closing
     * out the whole listing on the first sale. Only reaches 0 once every unit
     * has sold, at which point {@link #sold} flips true and the listing
     * leaves marketplace search; while &gt;= 1 the listing stays up with a
     * "×N left" badge. There's still no full cart/order flow — this is a
     * running count, not a line-itemed order history, so only the sale that
     * empties it records {@link #soldTo}/{@link #soldAt}.
     *
     * <p>{@code @ColumnDefault} matters here, not just {@code @Builder.Default}:
     * without it, adding this NOT NULL column to a table that already has
     * rows backfills every existing listing to MySQL's implicit int default
     * of 0 — a real bug hit once already (every pre-existing listing showed
     * "0 available" until corrected) — instead of the sensible "at least 1".
     */
    @Column(nullable = false)
    @org.hibernate.annotations.ColumnDefault("1")
    @Builder.Default
    private int quantity = 1;

    /**
     * Every photo attached to this listing, in upload order — an
     * {@code @ElementCollection} rather than a full entity since nothing
     * beyond "which URLs, in what order" is ever needed. Backed by a
     * separate {@code product_images} table (one row per photo) instead of
     * a single column, so a listing can carry more than one photo.
     */
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "image_url", length = 500)
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * Seller-declared "no longer for sale" — distinct from {@link #active}
     * (which only ever means "not removed by an admin"). A sold listing
     * stays active/visible on its own detail page (so the buyer can still
     * see it and leave a rating) but is excluded from marketplace search
     * results, and shows a "Sold" badge instead of an active listing.
     */
    @Column(name = "sold", nullable = false)
    @Builder.Default
    private boolean sold = false;

    @Column(name = "sold_at")
    private LocalDateTime soldAt;

    /**
     * Optional — the seller can name who they sold it to (picked from
     * people who messaged them about this listing) so the app can credit
     * that specific trade; left null if they don't specify.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sold_to_user_id")
    private User soldTo;

    // US2.4: incremented each time a shopper opens the listing's detail page
    // (ProductService#getActiveById) — not incremented by search/list results.
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private long viewCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
