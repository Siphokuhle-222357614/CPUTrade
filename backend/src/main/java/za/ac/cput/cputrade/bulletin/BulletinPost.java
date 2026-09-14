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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
