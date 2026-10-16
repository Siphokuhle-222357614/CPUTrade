package za.ac.cput.cputrade.marketplace;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import za.ac.cput.cputrade.chat.ChatMessageRepository;
import za.ac.cput.cputrade.user.Role;

import java.util.List;

import static com.jayway.jsonpath.JsonPath.read;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatFlowIntegrationTest extends MarketplaceFlowTestSupport {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Test
    void buyerAndSellerCanExchangeMessagesButAnUnrelatedUserCannotReadTheThread() throws Exception {
        TestUser seller = createUser(Role.VENDOR);
        TestUser buyer = createUser(Role.STUDENT);
        TestUser stranger = createUser(Role.STUDENT);
        long listingId = createListing(seller, "Test listing for buyer-seller chat");

        ResponseEntity<String> conversationResponse = restTemplate.postForEntity(
                "/api/products/{id}/conversations", authRequest(buyer), String.class, listingId);
        assertEquals(HttpStatus.CREATED, conversationResponse.getStatusCode());
        long conversationId = responseId(conversationResponse);

        ResponseEntity<String> buyerMessage = restTemplate.postForEntity(
                "/api/conversations/{id}/messages",
                jsonRequest(buyer, "{\"body\":\"Is this still available?\"}"),
                String.class,
                conversationId);
        assertEquals(HttpStatus.CREATED, buyerMessage.getStatusCode());
        assertEquals("Is this still available?", read(buyerMessage.getBody(), "$.body"));

        ResponseEntity<String> sellerMessage = restTemplate.postForEntity(
                "/api/conversations/{id}/messages",
                jsonRequest(seller, "{\"body\":\"Yes, I can meet at Bellville campus.\"}"),
                String.class,
                conversationId);
        assertEquals(HttpStatus.CREATED, sellerMessage.getStatusCode());

        ResponseEntity<String> messages = restTemplate.exchange(
                "/api/conversations/{id}/messages", HttpMethod.GET, authRequest(buyer), String.class, conversationId);
        assertEquals(HttpStatus.OK, messages.getStatusCode());
        assertEquals(2, ((List<?>) read(messages.getBody(), "$")).size());
        assertEquals("Yes, I can meet at Bellville campus.", read(messages.getBody(), "$[1].body"));

        ResponseEntity<String> forbiddenRead = restTemplate.exchange(
                "/api/conversations/{id}/messages", HttpMethod.GET, authRequest(stranger), String.class, conversationId);
        assertEquals(HttpStatus.FORBIDDEN, forbiddenRead.getStatusCode());

        var savedMessages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        assertEquals(2, savedMessages.size());
        assertEquals("Is this still available?", savedMessages.get(0).getBody());
    }
}
