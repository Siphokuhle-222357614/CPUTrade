package za.ac.cput.cputrade.rating;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.ac.cput.cputrade.product.Product;
import za.ac.cput.cputrade.user.User;

import java.time.LocalDateTime;

/**
 * A buyer's rating of a seller after a trade on a given listing (US6.1,
 * US6.2). One rating per (product, rater) — enforced both by a unique
 * constraint here and a pre-check in {@code RatingService}.
 */
@Entity
@Table(name = "ratings", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "rater_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rater_id", nullable = false)
    private User rater;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ratee_id", nullable = false)
    private User ratee;

    @Column(nullable = false)
    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
