package za.ac.cput.cputrade.product;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Composable filter predicates for product search (US3.1 category, US3.2
 * keyword, US3.3 price range) — combined conditionally in
 * {@link ProductService} so only the filters actually supplied are applied.
 */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<Product> hasCategory(Category category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    /** Free-text match across title and description (US3.2). */
    public static Specification<Product> keywordMatches(String keyword) {
        String like = "%" + keyword.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("description")), like)
        );
    }

    public static Specification<Product> priceGte(BigDecimal min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    public static Specification<Product> priceLte(BigDecimal max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), max);
    }
}
