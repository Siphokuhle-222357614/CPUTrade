package za.ac.cput.cputrade.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * Returns a JSON 403 for an authenticated user who lacks the required role
 * (e.g. a STUDENT hitting {@code /api/admin/**}) — without this, Spring
 * Security had no configured {@code AccessDeniedHandler} and fell back to
 * {@link JwtAuthEntryPoint}'s 401 "missing/invalid credentials" response,
 * which is misleading for a request that was actually authenticated fine.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", 403,
                "error", "Forbidden",
                "message", "You don't have permission to do that",
                "path", request.getRequestURI()
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
