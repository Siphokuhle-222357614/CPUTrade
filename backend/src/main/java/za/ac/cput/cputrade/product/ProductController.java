package za.ac.cput.cputrade.product;

import jakarta.validation.Valid;
import za.ac.cput.cputrade.product.dto.ProductCreateRequest;
import za.ac.cput.cputrade.product.dto.ProductResponse;
import za.ac.cput.cputrade.product.dto.ProductUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> list(@RequestParam(required = false) Category category) {
        return ResponseEntity.ok(productService.listActive(category));
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
}
