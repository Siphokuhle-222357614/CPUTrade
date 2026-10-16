package za.ac.cput.cputrade.order.dto;

import lombok.Builder;
import lombok.Getter;
import za.ac.cput.cputrade.order.OrderStatus;
import za.ac.cput.cputrade.order.PaymentStatus;
import za.ac.cput.cputrade.order.PurchaseOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PurchaseOrderResponse {

    private Long id;
    private Long productId;
    private Long buyerId;
    private Long sellerId;
    private String listingTitle;
    private String buyerUsername;
    private String sellerUsername;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal totalAmount;
    private PaymentStatus paymentStatus;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime receivedAt;
    private LocalDateTime cancelledAt;
    private boolean buyerRated;

    public static PurchaseOrderResponse from(PurchaseOrder order) {
        return from(order, false);
    }

    public static PurchaseOrderResponse from(PurchaseOrder order, boolean buyerRated) {
        return PurchaseOrderResponse.builder()
                .id(order.getId())
                .productId(order.getProduct().getId())
                .buyerId(order.getBuyer().getId())
                .sellerId(order.getSeller().getId())
                .listingTitle(order.getListingTitle())
                .buyerUsername(order.getBuyer().getDisplayName())
                .sellerUsername(order.getSeller().getDisplayName())
                .unitPrice(order.getUnitPrice())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .paymentStatus(order.getPaymentStatus())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .receivedAt(order.getReceivedAt())
                .cancelledAt(order.getCancelledAt())
                .buyerRated(buyerRated)
                .build();
    }
}
