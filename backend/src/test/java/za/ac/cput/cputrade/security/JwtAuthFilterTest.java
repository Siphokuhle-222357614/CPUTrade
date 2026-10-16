package za.ac.cput.cputrade.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthFilterTest {

    private static final String SECRET = "a-test-secret-that-is-long-enough-for-hmac-sha-256";

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void databaseFailuresDuringUserLookupAreNotTreatedAsInvalidTokens() throws Exception {
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        when(userDetailsService.loadUserByUsername("student"))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));
        JwtAuthFilter filter = new JwtAuthFilter(new JwtUtil(SECRET, 60_000), userDetailsService);
        MockHttpServletRequest request = bearerRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThrows(DataAccessResourceFailureException.class,
                () -> filter.doFilterInternal(request, response, new MockFilterChain()));
    }

    @Test
    void tokensForMissingUsersContinueAsUnauthenticated() throws ServletException, IOException {
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        when(userDetailsService.loadUserByUsername("student"))
                .thenThrow(new UsernameNotFoundException("user no longer exists"));
        JwtAuthFilter filter = new JwtAuthFilter(new JwtUtil(SECRET, 60_000), userDetailsService);
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(bearerRequest(), new MockHttpServletResponse(), chain);

        verify(userDetailsService).loadUserByUsername("student");
        org.junit.jupiter.api.Assertions.assertNotNull(chain.getRequest());
        org.junit.jupiter.api.Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private MockHttpServletRequest bearerRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + Jwts.builder()
                .subject("student")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact());
        return request;
    }
}
