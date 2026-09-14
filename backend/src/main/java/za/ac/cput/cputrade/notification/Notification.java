package za.ac.cput.cputrade.notification;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.ac.cput.cputrade.user.User;

import java.time.LocalDateTime;

/**
 * An in-app notification (US4.3) — vendor-approval, listing-removed, and
 * new-chat-message events all land here. This is purely in-app; it doesn't
 * replace {@code EmailNotifier}, which still logs its own stub "email" for
 * the same events.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 255)
    private String message;

    /** Frontend route to open when the notification is clicked, e.g. "/chats/5". Nullable. */
    @Column(length = 255)
    private String link;

    // Mapped to "is_read" — "read" is a reserved word in MySQL/MariaDB (READ
    // LOCK / LOCK TABLES ... READ) and breaks CREATE TABLE unquoted, the same
    // way "condition" did on the products table.
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
