package za.ac.cput.cputrade.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/** Returns a JSON 401 body instead of the default HTML error page (US1.4). */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    // Injects Spring Boot's own auto-configured ObjectMapper bean (Jackson 3,
    // `tools.jackson.*`, as of Spring Boot 4) instead of constructing one —
    // keeps this in sync with whatever JSON settings the app already uses.
    private final ObjectMapper objectMapper;

    public JwtAuthEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", 401,
                "error", "Unauthorized",
                "message", "Missing, invalid, or expired credentials",
                "path", request.getRequestURI()
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
