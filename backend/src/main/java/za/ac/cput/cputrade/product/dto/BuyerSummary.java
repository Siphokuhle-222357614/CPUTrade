package za.ac.cput.cputrade.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** One entry in the "who messaged you about this listing" dropdown a seller sees when marking it sold. */
@Getter
@Builder
@AllArgsConstructor
public class BuyerSummary {
    private Long id;
    private String username;
}
