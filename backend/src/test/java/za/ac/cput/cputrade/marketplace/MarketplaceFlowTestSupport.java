package za.ac.cput.cputrade.marketplace;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import za.ac.cput.cputrade.product.ProductRepository;
import za.ac.cput.cputrade.security.JwtUtil;
import za.ac.cput.cputrade.user.AccountStatus;
import za.ac.cput.cputrade.user.Role;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;

import java.util.UUID;

import static com.jayway.jsonpath.JsonPath.read;

@AutoConfigureTestRestTemplate
@ActiveProfiles({"local", "test"})
abstract class MarketplaceFlowTestSupport {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected JwtUtil jwtUtil;

    protected TestUser createUser(Role role) {
        String username = "flow_" + UUID.randomUUID().toString().replace("-", "");
        User user = userRepository.saveAndFlush(User.builder()
                .username(username)
                .email(username + "@mycput.ac.za")
                .campusHandle(username + "@cputmarket.ac.za")
                .passwordHash("unused-in-bearer-token-test")
                .role(role)
                .verified(true)
                .vendorApproved(true)
                .accountStatus(AccountStatus.ACTIVE)
                .build());
        return new TestUser(user.getId(), username, jwtUtil.generateToken(username));
    }

    protected long createListing(TestUser seller, String title) {
        return createListing(seller, title, 1);
    }

    protected long createListing(TestUser seller, String title, int quantity) {
        String body = """
                {
                  "title": "%s",
                  "description": "Created by a marketplace flow test",
                  "price": 125.00,
                  "category": "ELECTRONICS",
                  "condition": "GOOD",
                  "campus": "BELLVILLE",
                  "quantity": %d,
                  "openToSwap": false
                }
                """.formatted(title, quantity);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/products", jsonRequest(seller, body), String.class);
        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new AssertionError("Listing creation returned " + response.getStatusCode() + ": " + response.getBody());
        }
        return responseId(response);
    }

    protected long responseId(ResponseEntity<String> response) {
        Number id = read(response.getBody(), "$.id");
        return id.longValue();
    }

    protected static HttpEntity<String> jsonRequest(TestUser user, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (user != null) {
            headers.setBearerAuth(user.token());
        }
        return new HttpEntity<>(body, headers);
    }

    protected static HttpEntity<Void> authRequest(TestUser user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.token());
        return new HttpEntity<>(headers);
    }

    protected record TestUser(long id, String username, String token) {
    }
}
