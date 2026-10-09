package za.ac.cput.cputrade.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductionSecretsCheckTest {

    private static final String GOOD_SECRET = "a-real-long-random-secret-value-1234567890";

    @Test
    void acceptsRealSecrets() {
        assertDoesNotThrow(() -> new ProductionSecretsCheck(GOOD_SECRET, "S0meRealDbPass", "real-vapid-key"));
    }

    @Test
    void rejectsEveryDevDefault() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> new ProductionSecretsCheck(
                ProductionSecretsCheck.DEV_JWT_SECRET, ProductionSecretsCheck.DEV_DB_PASSWORD, ProductionSecretsCheck.DEV_VAPID_PRIVATE_KEY));
        assertTrue(e.getMessage().contains("JWT_SECRET"));
        assertTrue(e.getMessage().contains("DB_PASSWORD"));
        assertTrue(e.getMessage().contains("VAPID_PRIVATE_KEY"));
    }

    @Test
    void rejectsAShortJwtSecret() {
        assertThrows(IllegalStateException.class, () -> new ProductionSecretsCheck("short", "S0meRealDbPass", "real-vapid-key"));
    }
}
