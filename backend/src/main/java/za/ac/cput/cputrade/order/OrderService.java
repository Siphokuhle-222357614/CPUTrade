package za.ac.cput.cputrade.order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.notification.NotificationService;
import za.ac.cput.cputrade.notification.NotificationType;
import za.ac.cput.cputrade.order.dto.PurchaseOrderResponse;
import za.ac.cput.cputrade.product.Product;
import za.ac.cput.cputrade.product.ProductRepository;
import za.ac.cput.cputrade.rating.RatingRepository;
import za.ac.cput.cputrade.user.Role;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    private final ProductRepository productRepository;
    private final PurchaseOrderRepository orderRepository;
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final boolean simulatedPaymentEnabled;

    public OrderService(ProductRepository productRepository,
                        PurchaseOrderRepository orderRepository,
                        RatingRepository ratingRepository,
                        UserRepository userRepository,
                        NotificationService notificationService,
                        @Value("${app.checkout.simulated-payment-enabled:false}") boolean simulatedPaymentEnabled) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.simulatedPaymentEnabled = simulatedPaymentEnabled;
    }

    @Transactional
    public PurchaseOrderResponse checkout(Long productId, Authentication auth) {
        if (!simulatedPaymentEnabled) {
            throw ApiException.forbidden("Simulated checkout is only available in local mode");
        }

        User buyer = currentUser(auth);
        if (buyer.getRole() == Role.ADMIN) {
            throw ApiException.forbidden("Administrators cannot place marketplace orders");
        }
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> ApiException.notFound("Listing not found"));

        if (!product.isActive()) {
            throw ApiException.notFound("Listing not found");
        }
        if (product.isSold() || product.getQuantity() < 1) {
            throw ApiException.conflict("This listing is no longer available");
        }
        if (product.getSeller().getId().equals(buyer.getId())) {
            throw ApiException.badRequest("You cannot order your own listing");
        }

        PurchaseOrder order = orderRepository.saveAndFlush(PurchaseOrder.builder()
                .product(product)
                .buyer(buyer)
                .seller(product.getSeller())
                .listingTitle(product.getTitle())
                .unitPrice(product.getPrice())
                .quantity(1)
                .totalAmount(product.getPrice())
                .paymentStatus(PaymentStatus.SIMULATED)
                .status(OrderStatus.PLACED)
                .build());

        int remaining = product.getQuantity() - 1;
        product.setQuantity(remaining);
        if (remaining == 0) {
            product.setSold(true);
            product.setSoldAt(LocalDateTime.now());
            product.setSoldTo(buyer);
            product.setBuyerConfirmed(false);
            product.setBuyerConfirmedAt(null);
        }
        productRepository.save(product);

        return PurchaseOrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> listMine(Authentication auth) {
        User user = currentUser(auth);
        Set<Long> ratedProductIds = Set.copyOf(ratingRepository.findProductIdsByRaterId(user.getId()));
        return orderRepository.findByBuyerIdOrSellerIdOrderByCreatedAtDesc(user.getId(), user.getId())
                .stream()
                .map(order -> toResponse(order, user, ratedProductIds))
                .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getOne(Long orderId, Authentication auth) {
        User user = currentUser(auth);
        PurchaseOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found"));
        requireParticipant(order, user);
        boolean buyerRated = order.getBuyer().getId().equals(user.getId())
                && ratingRepository.existsByProductIdAndRaterId(order.getProduct().getId(), user.getId());
        return PurchaseOrderResponse.from(order, buyerRated);
    }

    @Transactional
    public PurchaseOrderResponse confirmReceived(Long orderId, Authentication auth) {
        User buyer = currentUser(auth);
        PurchaseOrder order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found"));
        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw ApiException.forbidden("Only the buyer can confirm receipt of this order");
        }
        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.READY_FOR_HANDOFF) {
            throw ApiException.conflict("Only active orders can have receipt confirmed");
        }
        if (order.getReceivedAt() != null) {
            throw ApiException.conflict("Receipt has already been confirmed for this order");
        }

        LocalDateTime receivedAt = LocalDateTime.now();
        order.setReceivedAt(receivedAt);
        order.setStatus(OrderStatus.RECEIVED);

        Product product = order.getProduct();
        if (product.isSold() && product.getSoldTo() != null
                && product.getSoldTo().getId().equals(buyer.getId())) {
            product.setBuyerConfirmed(true);
            product.setBuyerConfirmedAt(receivedAt);
            productRepository.save(product);
        }

        return PurchaseOrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public PurchaseOrderResponse cancel(Long orderId, Authentication auth) {
        User buyer = currentUser(auth);
        PurchaseOrder order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found"));
        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw ApiException.forbidden("Only the buyer can cancel this order");
        }
        if ((order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.READY_FOR_HANDOFF)
                || order.getReceivedAt() != null) {
            throw ApiException.conflict("Only orders awaiting receipt can be cancelled");
        }

        Product product = productRepository.findByIdForUpdate(order.getProduct().getId())
                .orElseThrow(() -> ApiException.notFound("Listing not found"));
        product.setQuantity(product.getQuantity() + order.getQuantity());
        if (product.isSold() && product.getQuantity() > 0) {
            product.setSold(false);
            product.setSoldAt(null);
            product.setSoldTo(null);
            product.setBuyerConfirmed(false);
            product.setBuyerConfirmedAt(null);
        }
        productRepository.save(product);

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        PurchaseOrder cancelledOrder = orderRepository.save(order);
        notificationService.create(order.getSeller(), NotificationType.ORDER_CANCELLED,
                buyer.getDisplayName() + " cancelled the simulated order for \"" + order.getListingTitle() + "\"",
                "/orders");
        return PurchaseOrderResponse.from(cancelledOrder);
    }

    @Transactional
    public PurchaseOrderResponse markReadyForHandoff(Long orderId, Authentication auth) {
        User seller = currentUser(auth);
        PurchaseOrder order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found"));
        if (!order.getSeller().getId().equals(seller.getId())) {
            throw ApiException.forbidden("Only the seller can update this order");
        }
        if (order.getStatus() != OrderStatus.PLACED) {
            throw ApiException.conflict("Only placed orders can be marked ready for hand-off");
        }

        order.setStatus(OrderStatus.READY_FOR_HANDOFF);
        PurchaseOrder readyOrder = orderRepository.save(order);
        notificationService.create(order.getBuyer(), NotificationType.ORDER_READY_FOR_HANDOFF,
                seller.getDisplayName() + " marked order #" + order.getId() + " ready for hand-off",
                "/orders");
        return PurchaseOrderResponse.from(readyOrder);
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder order, User viewer, Set<Long> ratedProductIds) {
        boolean buyerRated = order.getBuyer().getId().equals(viewer.getId())
                && ratedProductIds.contains(order.getProduct().getId());
        return PurchaseOrderResponse.from(order, buyerRated);
    }

    private void requireParticipant(PurchaseOrder order, User user) {
        if (!order.getBuyer().getId().equals(user.getId())
                && !order.getSeller().getId().equals(user.getId())) {
            throw ApiException.forbidden("You cannot access this order");
        }
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> ApiException.unauthorized("Unknown user"));
    }
}
