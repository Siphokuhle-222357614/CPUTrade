package za.ac.cput.cputrade.profile;

import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.product.ProductRepository;
import za.ac.cput.cputrade.profile.dto.PresenceResponse;
import za.ac.cput.cputrade.profile.dto.PublicProfileResponse;
import za.ac.cput.cputrade.rating.RatingService;
import za.ac.cput.cputrade.rating.dto.RatingSummary;
import za.ac.cput.cputrade.trust.SellerTrust;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserProfileService {

    /** No real presence channel (no WebSocket) — "online" is just "active very recently". */
    private static final int ONLINE_WINDOW_SECONDS = 60;

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final RatingService ratingService;

    public UserProfileService(UserRepository userRepository, ProductRepository productRepository, RatingService ratingService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.ratingService = ratingService;
    }

    public PublicProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        RatingSummary rating = ratingService.summarize(userId);
        long completedSales = productRepository.countBySellerIdAndSoldTrue(userId);

        return PublicProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .campusHandle(user.getCampusHandle())
                .role(user.getRole())
                .memberSince(user.getCreatedAt())
                .ratingAverage(rating.average())
                .ratingCount(rating.count())
                .activeListingCount(productRepository.countBySellerIdAndActiveTrue(userId))
                .completedSalesCount(completedSales)
                .verifiedSeller(SellerTrust.isVerified(completedSales))
                .online(isOnline(user))
                .lastActiveAt(user.getLastActiveAt())
                .build();
    }

    public PresenceResponse getPresence(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        return PresenceResponse.builder()
                .online(isOnline(user))
                .lastActiveAt(user.getLastActiveAt())
                .build();
    }

    private boolean isOnline(User user) {
        return user.getLastActiveAt() != null
                && user.getLastActiveAt().isAfter(LocalDateTime.now().minusSeconds(ONLINE_WINDOW_SECONDS));
    }
}
