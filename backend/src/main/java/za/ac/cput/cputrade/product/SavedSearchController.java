package za.ac.cput.cputrade.product;

import jakarta.validation.Valid;
import za.ac.cput.cputrade.product.dto.SavedSearchRequest;
import za.ac.cput.cputrade.product.dto.SavedSearchResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** "Notify me when..." standing search alerts — every route here requires auth (SecurityConfig's default rule). */
@RestController
@RequestMapping("/api/saved-searches")
public class SavedSearchController {

    private final SavedSearchService savedSearchService;

    public SavedSearchController(SavedSearchService savedSearchService) {
        this.savedSearchService = savedSearchService;
    }

    @GetMapping
    public ResponseEntity<List<SavedSearchResponse>> mySavedSearches(Authentication auth) {
        return ResponseEntity.ok(savedSearchService.mySavedSearches(auth));
    }

    @PostMapping
    public ResponseEntity<SavedSearchResponse> create(@Valid @RequestBody SavedSearchRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSearchService.create(request, auth));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        savedSearchService.delete(id, auth);
        return ResponseEntity.noContent().build();
    }
}
