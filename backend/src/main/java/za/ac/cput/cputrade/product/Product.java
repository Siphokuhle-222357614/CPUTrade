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

/**
 * A product listing (US2.1, US2.2, US3.1, US6.3).
 *
 * <p>{@code imageBase64} is validated (JPEG/PNG, decoded size &lt;= 500KB) in
 * {@code ProductService} before this entity is ever persisted — the decoded
 * byte length is what's checked, since Base64 inflates size by ~33%.
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

    @Lob
    @Column(name = "image_base64", columnDefinition = "LONGTEXT")
    private String imageBase64;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

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
