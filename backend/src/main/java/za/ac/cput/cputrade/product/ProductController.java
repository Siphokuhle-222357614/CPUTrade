package za.ac.cput.cputrade.product;

import jakarta.validation.Valid;
import za.ac.cput.cputrade.product.dto.AddImagesRequest;
import za.ac.cput.cputrade.product.dto.BuyerSummary;
import za.ac.cput.cputrade.product.dto.MarkSoldRequest;
import za.ac.cput.cputrade.product.dto.ProductCreateRequest;
import za.ac.cput.cputrade.product.dto.ProductResponse;
import za.ac.cput.cputrade.product.dto.ProductUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> list(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        return ResponseEntity.ok(productService.search(category, keyword, minPrice, maxPrice));
    }

    /** The current user's own business dashboard — every listing they own, active or not. Must come before /{id}. */
    @GetMapping("/mine")
    public ResponseEntity<List<ProductResponse>> listMine(Authentication auth) {
        return ResponseEntity.ok(productService.listMine(auth));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getActiveById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request, auth));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request, Authentication auth) {
        return ResponseEntity.ok(productService.update(id, request, auth));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        productService.delete(id, auth);
        return ResponseEntity.noContent().build();
    }

    /** Seller-only: who has messaged them about this listing — for the "mark as sold to" picker. Must precede /{id}. */
    @GetMapping("/{id}/interested-buyers")
    public ResponseEntity<List<BuyerSummary>> interestedBuyers(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(productService.interestedBuyers(id, auth));
    }

    @PatchMapping("/{id}/mark-sold")
    public ResponseEntity<ProductResponse> markSold(@PathVariable Long id, @RequestBody(required = false) MarkSoldRequest request, Authentication auth) {
        return ResponseEntity.ok(productService.markSold(id, request != null ? request : new MarkSoldRequest(), auth));
    }

    @PatchMapping("/{id}/mark-available")
    public ResponseEntity<ProductResponse> markAvailable(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(productService.markAvailable(id, auth));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ProductResponse> addImages(@PathVariable Long id, @Valid @RequestBody AddImagesRequest request, Authentication auth) {
        return ResponseEntity.ok(productService.addImages(id, request, auth));
    }

    @DeleteMapping("/{id}/images")
    public ResponseEntity<ProductResponse> removeImage(@PathVariable Long id, @RequestParam String url, Authentication auth) {
        return ResponseEntity.ok(productService.removeImage(id, url, auth));
    }
}
