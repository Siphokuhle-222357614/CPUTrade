package za.ac.cput.cputrade.chat;

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
 * A single buyer's message thread with a seller about one listing (US4.1).
 * {@code seller} is denormalized from {@code product.seller} at creation
 * time purely to make authorization/list queries simple — it never changes
 * independently of the product.
 */
@Entity
@Table(name = "conversations", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "buyer_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
