package za.ac.cput.cputrade.marketplace;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import za.ac.cput.cputrade.user.Role;

import java.util.UUID;

import static com.jayway.jsonpath.JsonPath.read;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ListingFlowIntegrationTest extends MarketplaceFlowTestSupport {

    @Test
    void vendorCanCreateAndBrowseListingWhileAnonymousAndOtherUserWritesAreRejected() {
        TestUser seller = createUser(Role.VENDOR);
        TestUser otherStudent = createUser(Role.STUDENT);
        String title = "Test listing for marketplace creation - "
                + UUID.randomUUID().toString().substring(0, 8);
        String listingJson = """
                {
                  "title": "%s",
                  "description": "Updated by a marketplace flow test",
                  "price": 99.50,
                  "category": "ELECTRONICS",
                  "condition": "LIKE_NEW",
                  "campus": "DISTRICT_SIX",
                  "quantity": 1,
                  "openToSwap": false
                }
                """.formatted(title);

        ResponseEntity<String> anonymousCreate = restTemplate.postForEntity(
                "/api/products", jsonRequest(null, listingJson), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, anonymousCreate.getStatusCode());

        long listingId = createListing(seller, title);

        ResponseEntity<String> search = restTemplate.getForEntity(
                "/api/products?keyword={keyword}", String.class, title);
        assertEquals(HttpStatus.OK, search.getStatusCode());
        assertEquals(listingId, ((Number) read(search.getBody(), "$[0].id")).longValue());
        assertEquals(title, read(search.getBody(), "$[0].title"));

        ResponseEntity<String> mine = restTemplate.exchange(
                "/api/products/mine", HttpMethod.GET, authRequest(seller), String.class);
        assertEquals(HttpStatus.OK, mine.getStatusCode());
        assertEquals(listingId, ((Number) read(mine.getBody(), "$[0].id")).longValue());

        ResponseEntity<String> forbiddenUpdate = restTemplate.exchange(
                "/api/products/{id}", HttpMethod.PUT, jsonRequest(otherStudent, listingJson), String.class, listingId);
        assertEquals(HttpStatus.FORBIDDEN, forbiddenUpdate.getStatusCode());

        assertEquals(title, productRepository.findById(listingId).orElseThrow().getTitle());
        assertTrue(productRepository.findById(listingId).orElseThrow().isActive());
    }
}
