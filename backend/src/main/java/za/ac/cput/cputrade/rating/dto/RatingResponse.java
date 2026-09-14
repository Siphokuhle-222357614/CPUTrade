package za.ac.cput.cputrade.rating.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import za.ac.cput.cputrade.rating.Rating;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class RatingResponse {
    private Long id;
    private Long productId;
    private String raterUsername;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;

    public static RatingResponse from(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .productId(rating.getProduct().getId())
                .raterUsername(rating.getRater().getUsername())
                .score(rating.getScore())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}
