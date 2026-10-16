package za.ac.cput.cputrade.order;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.cputrade.order.dto.CheckoutRequest;
import za.ac.cput.cputrade.order.dto.PurchaseOrderResponse;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<PurchaseOrderResponse> checkout(
            @Valid @RequestBody CheckoutRequest request,
            Authentication auth
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.checkout(request.getProductId(), auth));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<PurchaseOrderResponse>> listMine(Authentication auth) {
        return ResponseEntity.ok(orderService.listMine(auth));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> getOne(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(orderService.getOne(id, auth));
    }

    @PatchMapping("/{id}/confirm-received")
    public ResponseEntity<PurchaseOrderResponse> confirmReceived(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(orderService.confirmReceived(id, auth));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PurchaseOrderResponse> cancel(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(orderService.cancel(id, auth));
    }

    @PatchMapping("/{id}/ready-for-handoff")
    public ResponseEntity<PurchaseOrderResponse> markReadyForHandoff(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(orderService.markReadyForHandoff(id, auth));
    }
}
