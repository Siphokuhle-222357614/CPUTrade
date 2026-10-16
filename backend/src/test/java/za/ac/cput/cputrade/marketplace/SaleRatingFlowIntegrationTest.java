package za.ac.cput.cputrade.marketplace;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import za.ac.cput.cputrade.rating.RatingRepository;
import za.ac.cput.cputrade.user.Role;

import java.util.UUID;

import static com.jayway.jsonpath.JsonPath.read;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SaleRatingFlowIntegrationTest extends MarketplaceFlowTestSupport {

    @Autowired
    private RatingRepository ratingRepository;

    @Test
    void namedBuyerMustConfirmReceiptBeforeRatingAndCannotSubmitADuplicateRating() throws Exception {
        TestUser seller = createUser(Role.VENDOR);
        TestUser buyer = createUser(Role.STUDENT);
        TestUser otherStudent = createUser(Role.STUDENT);
        long listingId = createListing(seller, "Test listing for sale and rating");

        ResponseEntity<String> conversationResponse = restTemplate.postForEntity(
                "/api/products/{id}/conversations", authRequest(buyer), String.class, listingId);
        assertEquals(HttpStatus.CREATED, conversationResponse.getStatusCode());
        long conversationId = responseId(conversationResponse);
        assertTrue(conversationId > 0);

        ResponseEntity<String> markedSold = restTemplate.exchange(
                "/api/products/{id}/mark-sold", HttpMethod.PATCH,
                jsonRequest(seller, "{\"soldToUserId\":%d}".formatted(buyer.id())),
                String.class, listingId);
        assertEquals(HttpStatus.OK, markedSold.getStatusCode());
        assertEquals(Boolean.TRUE, read(markedSold.getBody(), "$.sold"));
        assertEquals(buyer.id(), ((Number) read(markedSold.getBody(), "$.soldToUserId")).longValue());

        String ratingJson = "{\"score\":5,\"comment\":\"Smooth exchange\"}";
        ResponseEntity<String> otherBuyerRating = restTemplate.postForEntity(
                "/api/products/{id}/ratings", jsonRequest(otherStudent, ratingJson), String.class, listingId);
        assertEquals(HttpStatus.FORBIDDEN, otherBuyerRating.getStatusCode());

        ResponseEntity<String> earlyRating = restTemplate.postForEntity(
                "/api/products/{id}/ratings", jsonRequest(buyer, ratingJson), String.class, listingId);
        assertEquals(HttpStatus.BAD_REQUEST, earlyRating.getStatusCode());

        ResponseEntity<String> otherBuyerConfirmation = restTemplate.exchange(
                "/api/products/{id}/confirm-received", HttpMethod.PATCH,
                authRequest(otherStudent), String.class, listingId);
        assertEquals(HttpStatus.FORBIDDEN, otherBuyerConfirmation.getStatusCode());

        ResponseEntity<String> confirmation = restTemplate.exchange(
                "/api/products/{id}/confirm-received", HttpMethod.PATCH,
                authRequest(buyer), String.class, listingId);
        assertEquals(HttpStatus.OK, confirmation.getStatusCode());
        assertEquals(Boolean.TRUE, read(confirmation.getBody(), "$.buyerConfirmed"));

        ResponseEntity<String> rating = restTemplate.postForEntity(
                "/api/products/{id}/ratings", jsonRequest(buyer, ratingJson), String.class, listingId);
        assertEquals(HttpStatus.CREATED, rating.getStatusCode());
        assertEquals(5, ((Number) read(rating.getBody(), "$.score")).intValue());
        assertEquals("Smooth exchange", read(rating.getBody(), "$.comment"));

        ResponseEntity<String> duplicateRating = restTemplate.postForEntity(
                "/api/products/{id}/ratings", jsonRequest(buyer, ratingJson), String.class, listingId);
        assertEquals(HttpStatus.CONFLICT, duplicateRating.getStatusCode());

        assertTrue(ratingRepository.existsByProductIdAndRaterId(listingId, buyer.id()));
        var savedListing = productRepository.findById(listingId).orElseThrow();
        assertTrue(savedListing.isBuyerConfirmed());
        assertTrue(savedListing.isSold());
    }
}
