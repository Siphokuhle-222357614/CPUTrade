package za.ac.cput.cputrade.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.cputrade.user.AccountStatus;
import za.ac.cput.cputrade.user.Role;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void adminEndpointsRejectUnauthenticatedAndMalformedBearerTokens() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer malformed-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUsersCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(Role.STUDENT, AccountStatus.ACTIVE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminsCanAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(Role.ADMIN, AccountStatus.ACTIVE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void suspendedUsersAreRejectedEvenWithAValidToken() throws Exception {
        mockMvc.perform(get("/api/products/mine")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(Role.STUDENT, AccountStatus.SUSPENDED)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("Your account has been suspended. If you believe this is a mistake, submit an appeal."));
    }

    @Test
    void pendingVendorsCanReadTheirListingsButCannotCreateOne() throws Exception {
        String token = tokenFor(Role.VENDOR, AccountStatus.ACTIVE);

        mockMvc.perform(get("/api/products/mine")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    private String tokenFor(Role role, AccountStatus accountStatus) {
        String username = "security_" + UUID.randomUUID().toString().replace("-", "");
        User user = User.builder()
                .username(username)
                .email(username + "@mycput.ac.za")
                .campusHandle(username + "@cputmarket.ac.za")
                .passwordHash("not-used-by-bearer-auth")
                .role(role)
                .vendorApproved(role != Role.VENDOR)
                .accountStatus(accountStatus)
                .build();
        userRepository.saveAndFlush(user);
        return jwtUtil.generateToken(username);
    }
}
