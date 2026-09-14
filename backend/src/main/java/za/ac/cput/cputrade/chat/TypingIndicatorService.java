package za.ac.cput.cputrade.chat;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ephemeral "is typing" signal, kept purely in memory — there's no
 * WebSocket/push channel in this app, so the frontend pings this on
 * keystroke (debounced) and polls it to show "... is typing" for the other
 * participant. Losing this on a restart is fine; it's not data, just a
 * few-seconds-old UI hint.
 */
@Service
public class TypingIndicatorService {

    private static final long TYPING_WINDOW_SECONDS = 5;

    // key: "conversationId:userId" -> last time that user pinged "I'm typing" in that thread
    private final Map<String, Instant> lastTypingAt = new ConcurrentHashMap<>();

    public void markTyping(Long conversationId, Long userId) {
        lastTypingAt.put(key(conversationId, userId), Instant.now());
    }

    public boolean isTyping(Long conversationId, Long userId) {
        Instant last = lastTypingAt.get(key(conversationId, userId));
        return last != null && last.isAfter(Instant.now().minusSeconds(TYPING_WINDOW_SECONDS));
    }

    private String key(Long conversationId, Long userId) {
        return conversationId + ":" + userId;
    }
}
