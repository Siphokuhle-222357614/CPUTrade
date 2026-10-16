package za.ac.cput.cputrade.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRET = "a-test-secret-that-is-long-enough-for-hmac-sha-256";

    @Test
    void aFreshTokenIsValidForItsOwner() {
        JwtUtil jwt = new JwtUtil(SECRET, 60_000);
        String token = jwt.generateToken("thabo_s");
        assertEquals("thabo_s", jwt.extractUsername(token));
        assertTrue(jwt.isTokenValid(token, "thabo_s"));
    }

    @Test
    void aTokenIsNotValidForAnotherUser() {
        JwtUtil jwt = new JwtUtil(SECRET, 60_000);
        assertFalse(jwt.isTokenValid(jwt.generateToken("thabo_s"), "lerato_m"));
    }

    @Test
    void anExpiredTokenIsRejected() {
        JwtUtil jwt = new JwtUtil(SECRET, -1_000);
        assertFalse(jwt.isTokenValid(jwt.generateToken("thabo_s"), "thabo_s"));
    }

    @Test
    void aTokenSignedWithADifferentSecretIsRejected() {
        String forged = new JwtUtil("a-completely-different-secret-for-forging-tokens-1", 60_000).generateToken("admin");
        assertFalse(new JwtUtil(SECRET, 60_000).isTokenValid(forged, "admin"));
    }

    @Test
    void aMalformedTokenIsRejected() {
        assertFalse(new JwtUtil(SECRET, 60_000).isTokenValid("not-a-jwt", "thabo_s"));
    }
}
