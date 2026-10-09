package za.ac.cput.cputrade.security;

import za.ac.cput.cputrade.common.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * Throttles repeated failed logins: after {@code maxFailures} wrong-password attempts for the same
 * key inside {@code windowMillis}, further attempts get 429 until the window since the first
 * counted failure has passed. A successful login clears the key.
 *
 * <p>In-memory and per instance — fine for a single backend process. If this is ever scaled to
 * several instances, move the counters to a shared store (or the reverse proxy).
 */
@Component
public class LoginRateLimiter {

    static final int DEFAULT_MAX_FAILURES = 5;
    static final long DEFAULT_WINDOW_MILLIS = 15 * 60 * 1000L;
    private static final int PRUNE_THRESHOLD = 1000;

    private record Attempts(int failures, long windowStart) {
    }

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();
    private final int maxFailures;
    private final long windowMillis;
    private final LongSupplier clock;

    @Autowired
    public LoginRateLimiter() {
        this(DEFAULT_MAX_FAILURES, DEFAULT_WINDOW_MILLIS, System::currentTimeMillis);
    }

    LoginRateLimiter(int maxFailures, long windowMillis, LongSupplier clock) {
        this.maxFailures = maxFailures;
        this.windowMillis = windowMillis;
        this.clock = clock;
    }

    /** Throws 429 if this key is currently locked out. */
    public void checkAllowed(String key) {
        Attempts a = attempts.get(key);
        if (a == null) return;
        long now = clock.getAsLong();
        if (now - a.windowStart() >= windowMillis) {
            attempts.remove(key);
            return;
        }
        if (a.failures() >= maxFailures) {
            long minutes = Math.max(1, (a.windowStart() + windowMillis - now + 59_999) / 60_000);
            throw ApiException.tooManyRequests(
                    "Too many failed login attempts. Try again in " + minutes + " minute" + (minutes == 1 ? "" : "s") + ".");
        }
    }

    public void recordFailure(String key) {
        long now = clock.getAsLong();
        if (attempts.size() > PRUNE_THRESHOLD) prune(now);
        attempts.merge(key, new Attempts(1, now), (old, ignored) ->
                now - old.windowStart() >= windowMillis ? new Attempts(1, now) : new Attempts(old.failures() + 1, old.windowStart()));
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    private void prune(long now) {
        for (Iterator<Attempts> it = attempts.values().iterator(); it.hasNext(); ) {
            if (now - it.next().windowStart() >= windowMillis) it.remove();
        }
    }
}
