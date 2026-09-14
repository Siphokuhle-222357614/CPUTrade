package za.ac.cput.cputrade.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByActiveTrueOrderByCreatedAtDesc();

    List<Product> findByActiveTrueAndCategoryOrderByCreatedAtDesc(Category category);

    /** Admin moderation view — includes inactive listings (US6.3). */
    List<Product> findAllByOrderByCreatedAtDesc();
}
