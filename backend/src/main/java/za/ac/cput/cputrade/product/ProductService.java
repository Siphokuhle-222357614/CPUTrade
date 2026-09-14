package za.ac.cput.cputrade.product;

import za.ac.cput.cputrade.common.ImageValidator;
import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.product.dto.ProductCreateRequest;
import za.ac.cput.cputrade.product.dto.ProductResponse;
import za.ac.cput.cputrade.product.dto.ProductUpdateRequest;
import za.ac.cput.cputrade.user.Role;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<ProductResponse> listActive(Category category) {
        List<Product> products = category == null
                ? productRepository.findByActiveTrueOrderByCreatedAtDesc()
                : productRepository.findByActiveTrueAndCategoryOrderByCreatedAtDesc(category);
        return products.stream().map(ProductResponse::from).toList();
    }

    public ProductResponse getActiveById(Long id) {
        Product product = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> ApiException.notFound("Listing not found"));
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse create(ProductCreateRequest request, Authentication auth) {
        User seller = currentUser(auth);

        // US1.5: a VENDOR can register and log in immediately, but cannot sell until approved.
        if (seller.getRole() == Role.VENDOR && !seller.isVendorApproved()) {
            throw ApiException.forbidden("Your vendor account is pending admin approval");
        }

        String decodedImage = null;
        if (request.getImageBase64() != null && !request.getImageBase64().isBlank()) {
            ImageValidator.validate(request.getImageBase64()); // throws on invalid/oversized/wrong-type
            decodedImage = request.getImageBase64();
        }

        Product product = Product.builder()
                .seller(seller)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .condition(request.getCondition())
                .imageBase64(decodedImage)
                .active(true)
                .build();

        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request, Authentication auth) {
        Product product = findByIdOrThrow(id);
        requireOwnerOrAdmin(product, auth);

        if (request.getImageBase64() != null && !request.getImageBase64().isBlank()) {
            ImageValidator.validate(request.getImageBase64());
            product.setImageBase64(request.getImageBase64());
        }

        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setCondition(request.getCondition());

        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id, Authentication auth) {
        Product product = findByIdOrThrow(id);
        requireOwnerOrAdmin(product, auth);
        productRepository.delete(product);
    }

    private Product findByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Listing not found"));
    }

    private void requireOwnerOrAdmin(Product product, Authentication auth) {
        User user = currentUser(auth);
        boolean isOwner = product.getSeller().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw ApiException.forbidden("You may only modify your own listings");
        }
    }

    private User currentUser(Authentication auth) {
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.unauthorized("Unknown user"));
    }
}
