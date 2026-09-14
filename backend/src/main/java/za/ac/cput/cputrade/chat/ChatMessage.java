package za.ac.cput.cputrade.chat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.ac.cput.cputrade.user.User;

import java.time.LocalDateTime;

/** A single message within a {@link Conversation} (US4.1, US4.2). */
@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    /** Optional — set when the sender is suggesting a safe meetup spot (US4.2). */
    @Enumerated(EnumType.STRING)
    @Column(name = "location_suggestion", length = 20)
    private LocationSuggestion locationSuggestion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean edited = false;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    /**
     * Soft delete only — {@code body} is deliberately never cleared or
     * overwritten when this is set. The frontend renders a "message
     * deleted" placeholder instead of the real body once this is true (see
     * {@code ChatMessageResponse}), but the real content stays in the
     * database, e.g. for report/dispute evidence — a user can make a
     * message disappear from their chat view, not from the record.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    /** Set the first time the recipient's client fetches this message (single check -> double check, grey). */
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    /** Set when the recipient opens the conversation and it's marked read (double check turns blue). */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
