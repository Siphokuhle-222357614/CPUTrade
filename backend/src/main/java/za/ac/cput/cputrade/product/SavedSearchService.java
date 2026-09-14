package za.ac.cput.cputrade.product;

import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.notification.NotificationService;
import za.ac.cput.cputrade.notification.NotificationType;
import za.ac.cput.cputrade.product.dto.SavedSearchRequest;
import za.ac.cput.cputrade.product.dto.SavedSearchResponse;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SavedSearchService {

    private static final Logger log = LoggerFactory.getLogger(SavedSearchService.class);

    private final SavedSearchRepository savedSearchRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public SavedSearchService(SavedSearchRepository savedSearchRepository, UserRepository userRepository,
                               NotificationService notificationService) {
        this.savedSearchRepository = savedSearchRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public List<SavedSearchResponse> mySavedSearches(Authentication auth) {
        User user = currentUser(auth);
        return savedSearchRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(SavedSearchResponse::from)
                .toList();
    }

    @Transactional
    public SavedSearchResponse create(SavedSearchRequest request, Authentication auth) {
        User user = currentUser(auth);
        boolean hasKeyword = request.getKeyword() != null && !request.getKeyword().isBlank();
        if (!hasKeyword && request.getCategory() == null && request.getMaxPrice() == null) {
            throw ApiException.badRequest("Set at least a keyword, category, or max price to save this search");
        }

        SavedSearch savedSearch = SavedSearch.builder()
                .user(user)
                .keyword(hasKeyword ? request.getKeyword() : null)
                .category(request.getCategory())
                .maxPrice(request.getMaxPrice())
                .build();
        return SavedSearchResponse.from(savedSearchRepository.save(savedSearch));
    }

    @Transactional
    public void delete(Long id, Authentication auth) {
        User user = currentUser(auth);
        savedSearchRepository.deleteByIdAndUserId(id, user.getId());
    }

    /**
     * Called by ProductService right after a new listing is saved — checks
     * every standing alert in the system and notifies whoever's search
     * matches, except the seller themselves (checking your own new listing
     * against your own alert would be a strange, pointless notification).
     */
    public void notifyMatchingSavedSearches(Product product) {
        for (SavedSearch savedSearch : savedSearchRepository.findAll()) {
            if (savedSearch.getUser().getId().equals(product.getSeller().getId())) {
                continue;
            }
            if (!savedSearch.matches(product)) {
                continue;
            }
            try {
                notificationService.create(savedSearch.getUser(), NotificationType.SAVED_SEARCH_MATCH,
                        "New listing matches your alert: \"" + product.getTitle() + "\"",
                        "/products/" + product.getId());
            } catch (Exception e) {
                // Never let one failed alert (or all of them) roll back the listing
                // creation this runs inside of, or stop the rest from being checked.
                log.warn("Could not notify saved search {} about product {}", savedSearch.getId(), product.getId(), e);
            }
        }
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> ApiException.unauthorized("Unknown user"));
    }
}
