package za.ac.cput.cputrade.rating;

import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.product.Product;
import za.ac.cput.cputrade.product.ProductRepository;
import za.ac.cput.cputrade.rating.dto.RatingRequest;
import za.ac.cput.cputrade.rating.dto.RatingResponse;
import za.ac.cput.cputrade.rating.dto.RatingSummary;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public RatingService(RatingRepository ratingRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RatingResponse rate(Long productId, RatingRequest request, Authentication auth) {
        User rater = currentUser(auth);
        Product product = productRepository.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> ApiException.notFound("Listing not found"));
        User seller = product.getSeller();

        if (seller.getId().equals(rater.getId())) {
            throw ApiException.badRequest("You cannot rate your own listing");
        }
        if (!product.isSold()) {
            throw ApiException.badRequest("You can rate a seller once they've marked this listing as sold");
        }
        if (ratingRepository.existsByProductIdAndRaterId(productId, rater.getId())) {
            throw ApiException.conflict("You have already rated this listing");
        }

        Rating rating = Rating.builder()
                .product(product)
                .rater(rater)
                .ratee(seller)
                .score(request.getScore())
                .comment(request.getComment())
                .build();

        return RatingResponse.from(ratingRepository.save(rating));
    }

    /** Used by ProductService to embed a seller's average/count on listings. */
    public RatingSummary summarize(Long sellerId) {
        long count = ratingRepository.countByRateeId(sellerId);
        if (count == 0) {
            return RatingSummary.NONE;
        }
        Double average = ratingRepository.findAverageScoreByRateeId(sellerId);
        return new RatingSummary(average, count);
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> ApiException.unauthorized("Unknown user"));
    }
}
