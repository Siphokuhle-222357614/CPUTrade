package za.ac.cput.cputrade.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    boolean existsByProductIdAndRaterId(Long productId, Long raterId);

    @Query("select avg(r.score) from Rating r where r.ratee.id = :sellerId")
    Double findAverageScoreByRateeId(@Param("sellerId") Long sellerId);

    long countByRateeId(Long sellerId);
}
