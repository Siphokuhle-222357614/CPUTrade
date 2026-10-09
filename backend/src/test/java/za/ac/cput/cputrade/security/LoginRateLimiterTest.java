package za.ac.cput.cputrade.security;

import za.ac.cput.cputrade.common.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimiterTest {

    private final AtomicLong now = new AtomicLong(1_000_000);
    private final LoginRateLimiter limiter = new LoginRateLimiter(3, 60_000, now::get);

    @Test
    void allowsAttemptsUnderTheLimit() {
        limiter.recordFailure("ip|thabo");
        limiter.recordFailure("ip|thabo");
        assertDoesNotThrow(() -> limiter.checkAllowed("ip|thabo"));
    }

    @Test
    void blocksWithTooManyRequestsAtTheLimit() {
        for (int i = 0; i < 3; i++) limiter.recordFailure("ip|thabo");
        ApiException e = assertThrows(ApiException.class, () -> limiter.checkAllowed("ip|thabo"));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, e.getStatus());
    }

    @Test
    void oneKeyBeingBlockedDoesNotAffectAnother() {
        for (int i = 0; i < 3; i++) limiter.recordFailure("attacker|thabo");
        assertDoesNotThrow(() -> limiter.checkAllowed("student|thabo"));
    }

    @Test
    void unlocksAfterTheWindowPasses() {
        for (int i = 0; i < 3; i++) limiter.recordFailure("ip|thabo");
        now.addAndGet(60_000);
        assertDoesNotThrow(() -> limiter.checkAllowed("ip|thabo"));
    }

    @Test
    void successfulLoginClearsTheFailures() {
        limiter.recordFailure("ip|thabo");
        limiter.recordFailure("ip|thabo");
        limiter.recordSuccess("ip|thabo");
        limiter.recordFailure("ip|thabo");
        assertDoesNotThrow(() -> limiter.checkAllowed("ip|thabo"));
    }
}
