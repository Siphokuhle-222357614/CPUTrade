package za.ac.cput.cputrade.product.dto;

import lombok.Getter;
import lombok.Setter;

/** Optional — the seller can name who bought it (from people who messaged them about it); left null otherwise. */
@Getter
@Setter
public class MarkSoldRequest {
    private Long soldToUserId;
}
