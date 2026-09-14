package za.ac.cput.cputrade.bulletin;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.ac.cput.cputrade.user.User;

import java.time.LocalDateTime;

/**
 * A campus bulletin post (US5.1–US5.3) — a lightweight noticeboard separate
 * from marketplace listings, e.g. "looking for a study group" or "lost ID
 * card near IT Building". No price/category/image, unlike {@code Product}.
 */
@Entity
@Table(name = "bulletin_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulletinPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    /**
     * {@code @ColumnDefault} matters here, not just {@code @Builder.Default}:
     * Hibernate maps this to a native MySQL {@code ENUM(...)} column, and
     * MySQL's implicit default for a NOT NULL enum column added to a table
     * that already has rows is its *first value alphabetically* — which
     * silently backfilled every pre-existing bulletin post to FOUND instead
     * of GENERAL until this was caught and fixed.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @org.hibernate.annotations.ColumnDefault("'GENERAL'")
    @Builder.Default
    private BulletinType type = BulletinType.GENERAL;

    /** The poster's own "resolved" flag — e.g. a lost item was reunited with its owner. Purely cosmetic. */
    @Column(nullable = false)
    @Builder.Default
    private boolean resolved = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
