package za.ac.cput.cputrade.marketplace;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import za.ac.cput.cputrade.notification.NotificationRepository;
import za.ac.cput.cputrade.notification.NotificationType;
import za.ac.cput.cputrade.order.OrderStatus;
import za.ac.cput.cputrade.order.PaymentStatus;
import za.ac.cput.cputrade.order.PurchaseOrderRepository;
import za.ac.cput.cputrade.product.Product;
import za.ac.cput.cputrade.rating.RatingRepository;
import za.ac.cput.cputrade.user.Role;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.jayway.jsonpath.JsonPath.read;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderFlowIntegrationTest extends MarketplaceFlowTestSupport {

    @Autowired
    private PurchaseOrderRepository orderRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void buyerCanCheckoutConfirmReceiptAndRateWhileOrderHistoryIsProtected() throws Exception {
        TestUser seller = createUser(Role.VENDOR);
        TestUser buyer = createUser(Role.STUDENT);
        TestUser stranger = createUser(Role.STUDENT);
        TestUser admin = createUser(Role.ADMIN);
        String title = "OrderListing-" + UUID.randomUUID().toString().substring(0, 8);
        long listingId = createListing(seller, title, 2);
        String checkoutJson = "{\"productId\":%d}".formatted(listingId);

        ResponseEntity<String> anonymousCheckout = restTemplate.postForEntity(
                "/api/orders/checkout", jsonRequest(null, checkoutJson), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, anonymousCheckout.getStatusCode());

        ResponseEntity<String> ownerCheckout = restTemplate.postForEntity(
                "/api/orders/checkout", jsonRequest(seller, checkoutJson), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, ownerCheckout.getStatusCode());

        ResponseEntity<String> adminCheckout = restTemplate.postForEntity(
                "/api/orders/checkout", jsonRequest(admin, checkoutJson), String.class);
        assertEquals(HttpStatus.FORBIDDEN, adminCheckout.getStatusCode());

        ResponseEntity<String> placedOrder = restTemplate.postForEntity(
                "/api/orders/checkout", jsonRequest(buyer, checkoutJson), String.class);
        assertEquals(HttpStatus.CREATED, placedOrder.getStatusCode());
        long orderId = responseId(placedOrder);
        assertEquals(listingId, ((Number) read(placedOrder.getBody(), "$.productId")).longValue());
        assertEquals(title, read(placedOrder.getBody(), "$.listingTitle"));
        assertEquals(0, new BigDecimal(read(placedOrder.getBody(), "$.totalAmount").toString())
                .compareTo(new BigDecimal("125.00")));
        assertEquals(PaymentStatus.SIMULATED.name(), read(placedOrder.getBody(), "$.paymentStatus"));
        assertEquals(OrderStatus.PLACED.name(), read(placedOrder.getBody(), "$.status"));
        assertNotNull(read(placedOrder.getBody(), "$.createdAt"));

        Product listingAfterCheckout = productRepository.findById(listingId).orElseThrow();
        assertEquals(1, listingAfterCheckout.getQuantity());
        assertTrue(listingAfterCheckout.isActive());
        assertTrue(!listingAfterCheckout.isSold());

        ResponseEntity<String> buyerOrders = restTemplate.exchange(
                "/api/orders/mine", HttpMethod.GET, authRequest(buyer), String.class);
        assertEquals(HttpStatus.OK, buyerOrders.getStatusCode());
        List<Number> orderIds = read(buyerOrders.getBody(), "$[*].id");
        assertTrue(orderIds.stream().anyMatch(id -> id.longValue() == orderId));

        ResponseEntity<String> strangerOrder = restTemplate.exchange(
                "/api/orders/{id}", HttpMethod.GET, authRequest(stranger), String.class, orderId);
        assertEquals(HttpStatus.FORBIDDEN, strangerOrder.getStatusCode());

        ResponseEntity<String> earlyRating = restTemplate.postForEntity(
                "/api/products/{id}/ratings", jsonRequest(buyer, "{\"score\":5,\"comment\":\"Great\"}"),
                String.class, listingId);
        assertEquals(HttpStatus.BAD_REQUEST, earlyRating.getStatusCode());

        ResponseEntity<String> strangerConfirmation = restTemplate.exchange(
                "/api/orders/{id}/confirm-received", HttpMethod.PATCH,
                authRequest(stranger), String.class, orderId);
        assertEquals(HttpStatus.FORBIDDEN, strangerConfirmation.getStatusCode());

        ResponseEntity<String> receipt = restTemplate.exchange(
                "/api/orders/{id}/confirm-received", HttpMethod.PATCH,
                authRequest(buyer), String.class, orderId);
        assertEquals(HttpStatus.OK, receipt.getStatusCode());
        assertEquals(OrderStatus.RECEIVED.name(), read(receipt.getBody(), "$.status"));
        assertNotNull(read(receipt.getBody(), "$.receivedAt"));

        ResponseEntity<String> rating = restTemplate.postForEntity(
                "/api/products/{id}/ratings", jsonRequest(buyer, "{\"score\":5,\"comment\":\"Great\"}"),
                String.class, listingId);
        assertEquals(HttpStatus.CREATED, rating.getStatusCode());
        assertTrue(ratingRepository.existsByProductIdAndRaterId(listingId, buyer.id()));
        ResponseEntity<String> ratedOrder = restTemplate.exchange(
                "/api/orders/{id}", HttpMethod.GET, authRequest(buyer), String.class, orderId);
        assertEquals(Boolean.TRUE, read(ratedOrder.getBody(), "$.buyerRated"));

        ResponseEntity<String> cancelReceivedOrder = restTemplate.exchange(
                "/api/orders/{id}/cancel", HttpMethod.PATCH, authRequest(buyer), String.class, orderId);
        assertEquals(HttpStatus.CONFLICT, cancelReceivedOrder.getStatusCode());
        assertEquals(OrderStatus.RECEIVED,
                orderRepository.findById(orderId).orElseThrow().getStatus());

        ResponseEntity<String> deleteListing = restTemplate.exchange(
                "/api/products/{id}", HttpMethod.DELETE,
                authRequest(seller), String.class, listingId);
        assertEquals(HttpStatus.CONFLICT, deleteListing.getStatusCode());
        assertTrue(orderRepository.existsById(orderId));
    }

    @Test
    void concurrentAvailabilityIsRecheckedAndOutOfStockListingsCannotBeOrderedAgain() {
        TestUser seller = createUser(Role.VENDOR);
        TestUser firstBuyer = createUser(Role.STUDENT);
        TestUser secondBuyer = createUser(Role.STUDENT);
        long listingId = createListing(seller, "SingleUnit-" + UUID.randomUUID().toString().substring(0, 8));
        String checkoutJson = "{\"productId\":%d}".formatted(listingId);

        ResponseEntity<String> firstOrder = restTemplate.postForEntity(
                "/api/orders/checkout", jsonRequest(firstBuyer, checkoutJson), String.class);
        assertEquals(HttpStatus.CREATED, firstOrder.getStatusCode());

        ResponseEntity<String> secondOrder = restTemplate.postForEntity(
                "/api/orders/checkout", jsonRequest(secondBuyer, checkoutJson), String.class);
        assertEquals(HttpStatus.CONFLICT, secondOrder.getStatusCode());

        Product soldListing = productRepository.findById(listingId).orElseThrow();
        assertEquals(0, soldListing.getQuantity());
        assertTrue(soldListing.isSold());
        assertEquals(firstBuyer.id(), soldListing.getSoldTo().getId());
        assertEquals(1, orderRepository.countByProductId(listingId));
    }

    @Test
    void buyerCanCancelUnreceivedOrderOnceAndInventoryBecomesAvailableAgain() {
        TestUser seller = createUser(Role.VENDOR);
        TestUser buyer = createUser(Role.STUDENT);
        TestUser otherBuyer = createUser(Role.STUDENT);
        long listingId = createListing(seller, "CancelOrder-" + UUID.randomUUID().toString().substring(0, 8));
        String checkoutJson = "{\"productId\":%d}".formatted(listingId);
        ResponseEntity<String> placedOrder = restTemplate.postForEntity(
                "/api/orders/checkout", jsonRequest(buyer, checkoutJson), String.class);
        assertEquals(HttpStatus.CREATED, placedOrder.getStatusCode());
        long orderId = responseId(placedOrder);

        ResponseEntity<String> readyOrder = restTemplate.exchange(
                "/api/orders/{id}/ready-for-handoff", HttpMethod.PATCH,
                authRequest(seller), String.class, orderId);
        assertEquals(HttpStatus.OK, readyOrder.getStatusCode());
        assertEquals(OrderStatus.READY_FOR_HANDOFF.name(), read(readyOrder.getBody(), "$.status"));

        ResponseEntity<String> otherBuyerCancel = restTemplate.exchange(
                "/api/orders/{id}/cancel", HttpMethod.PATCH,
                authRequest(otherBuyer), String.class, orderId);
        assertEquals(HttpStatus.FORBIDDEN, otherBuyerCancel.getStatusCode());

        ResponseEntity<String> cancelledOrder = restTemplate.exchange(
                "/api/orders/{id}/cancel", HttpMethod.PATCH,
                authRequest(buyer), String.class, orderId);
        assertEquals(HttpStatus.OK, cancelledOrder.getStatusCode());
        assertEquals(OrderStatus.CANCELLED.name(), read(cancelledOrder.getBody(), "$.status"));
        assertNotNull(read(cancelledOrder.getBody(), "$.cancelledAt"));

        Product restoredListing = productRepository.findById(listingId).orElseThrow();
        assertEquals(1, restoredListing.getQuantity());
        assertTrue(!restoredListing.isSold());
        assertNull(restoredListing.getSoldTo());
        assertTrue(!restoredListing.isBuyerConfirmed());

        ResponseEntity<String> duplicateCancel = restTemplate.exchange(
                "/api/orders/{id}/cancel", HttpMethod.PATCH,
                authRequest(buyer), String.class, orderId);
        assertEquals(HttpStatus.CONFLICT, duplicateCancel.getStatusCode());
        assertEquals(1, productRepository.findById(listingId).orElseThrow().getQuantity());

        ResponseEntity<String> newOrder = restTemplate.postForEntity(
                "/api/orders/checkout", jsonRequest(otherBuyer, checkoutJson), String.class);
        assertEquals(HttpStatus.CREATED, newOrder.getStatusCode());
        assertEquals(0, productRepository.findById(listingId).orElseThrow().getQuantity());

        assertTrue(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(seller.id()).stream()
                .anyMatch(notification -> notification.getType() == NotificationType.ORDER_CANCELLED));
    }

    @Test
    void onlySellerCanMarkPlacedOrderReadyForHandoff() {
        TestUser seller = createUser(Role.VENDOR);
        TestUser buyer = createUser(Role.STUDENT);
        TestUser otherSeller = createUser(Role.VENDOR);
        long listingId = createListing(seller, "Handoff-" + UUID.randomUUID().toString().substring(0, 8));
        ResponseEntity<String> placedOrder = restTemplate.postForEntity(
                "/api/orders/checkout", jsonRequest(buyer, "{\"productId\":%d}".formatted(listingId)),
                String.class);
        assertEquals(HttpStatus.CREATED, placedOrder.getStatusCode());
        long orderId = responseId(placedOrder);

        ResponseEntity<String> unauthorizedUpdate = restTemplate.exchange(
                "/api/orders/{id}/ready-for-handoff", HttpMethod.PATCH,
                authRequest(otherSeller), String.class, orderId);
        assertEquals(HttpStatus.FORBIDDEN, unauthorizedUpdate.getStatusCode());

        ResponseEntity<String> readyOrder = restTemplate.exchange(
                "/api/orders/{id}/ready-for-handoff", HttpMethod.PATCH,
                authRequest(seller), String.class, orderId);
        assertEquals(HttpStatus.OK, readyOrder.getStatusCode());
        assertEquals(OrderStatus.READY_FOR_HANDOFF.name(), read(readyOrder.getBody(), "$.status"));

        ResponseEntity<String> duplicateUpdate = restTemplate.exchange(
                "/api/orders/{id}/ready-for-handoff", HttpMethod.PATCH,
                authRequest(seller), String.class, orderId);
        assertEquals(HttpStatus.CONFLICT, duplicateUpdate.getStatusCode());
        assertTrue(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(buyer.id()).stream()
                .anyMatch(notification -> notification.getType() == NotificationType.ORDER_READY_FOR_HANDOFF));

        ResponseEntity<String> receipt = restTemplate.exchange(
                "/api/orders/{id}/confirm-received", HttpMethod.PATCH,
                authRequest(buyer), String.class, orderId);
        assertEquals(HttpStatus.OK, receipt.getStatusCode());
        assertEquals(OrderStatus.RECEIVED.name(), read(receipt.getBody(), "$.status"));
    }
}
