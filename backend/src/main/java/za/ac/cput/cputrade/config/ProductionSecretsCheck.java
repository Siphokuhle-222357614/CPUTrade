package za.ac.cput.cputrade.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Refuses to start under the {@code prod} profile while any secret still has the dev-only value
 * that is committed in this repo (anyone who can read the repo knows it).
 *
 * <p>Local development is unaffected: it only runs when started with
 * {@code SPRING_PROFILES_ACTIVE=prod} (see DEPLOYMENT.md).
 */
@Component
@Profile("prod")
public class ProductionSecretsCheck {

    static final String DEV_JWT_SECRET = "cputrade-dev-secret-change-me-before-any-real-deployment-01234567";
    static final String DEV_DB_PASSWORD = "ChangeMe_StrongPass!";
    static final String DEV_VAPID_PRIVATE_KEY = "UfliEGc4OPv0rwnKa2-g7iN5cAYAR8-5BAo346_14M8";

    public ProductionSecretsCheck(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${spring.datasource.password}") String dbPassword,
            @Value("${push.vapid.private-key}") String vapidPrivateKey) {
        List<String> problems = problems(jwtSecret, dbPassword, vapidPrivateKey);
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Refusing to start in production with insecure settings: "
                    + String.join("; ", problems) + ". See DEPLOYMENT.md, section 1.");
        }
    }

    static List<String> problems(String jwtSecret, String dbPassword, String vapidPrivateKey) {
        List<String> problems = new ArrayList<>();
        if (DEV_JWT_SECRET.equals(jwtSecret)) problems.add("JWT_SECRET is the dev default");
        else if (jwtSecret == null || jwtSecret.length() < 32) problems.add("JWT_SECRET is shorter than 32 characters");
        if (DEV_DB_PASSWORD.equals(dbPassword)) problems.add("DB_PASSWORD is the dev default");
        if (DEV_VAPID_PRIVATE_KEY.equals(vapidPrivateKey)) problems.add("VAPID_PRIVATE_KEY is the dev default");
        return problems;
    }
}
