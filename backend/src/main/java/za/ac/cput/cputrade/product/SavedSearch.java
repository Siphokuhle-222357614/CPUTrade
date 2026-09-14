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
 * "Tell me when something like this gets posted" — a standing alert rather
 * than a one-off search. Every field but the owner is optional, but at least
 * one must be set (enforced in SavedSearchService, not here) — an empty
 * search would match every new listing and spam the very feature meant to
 * cut down on re-checking the marketplace.
 */
@Entity
@Table(name = "saved_searches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedSearch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 150)
    private String keyword;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Category category;

    @Column(name = "max_price", precision = 10, scale = 2)
    private BigDecimal maxPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /** Does a freshly-posted listing match this alert? Every set criterion must match; unset ones are ignored. */
    public boolean matches(Product product) {
        if (category != null && category != product.getCategory()) {
            return false;
        }
        if (maxPrice != null && product.getPrice().compareTo(maxPrice) > 0) {
            return false;
        }
        if (keyword != null && !keyword.isBlank()) {
            String needle = keyword.toLowerCase();
            String haystack = (product.getTitle() + " " + (product.getDescription() != null ? product.getDescription() : "")).toLowerCase();
            return haystack.contains(needle);
        }
        return true;
    }
}
