package za.ac.cput.cputrade.rating;

import jakarta.validation.Valid;
import za.ac.cput.cputrade.rating.dto.RatingRequest;
import za.ac.cput.cputrade.rating.dto.RatingResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Leaving a rating requires auth (US6.2); reading ratings is embedded on ProductResponse instead of a separate endpoint. */
@RestController
@RequestMapping("/api/products/{productId}/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public ResponseEntity<RatingResponse> rate(
            @PathVariable Long productId,
            @Valid @RequestBody RatingRequest request,
            Authentication auth
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.rate(productId, request, auth));
    }
}
