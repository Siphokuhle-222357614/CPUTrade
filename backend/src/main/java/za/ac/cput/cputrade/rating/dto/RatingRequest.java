package za.ac.cput.cputrade.rating.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer score;

    @Size(max = 1000)
    private String comment;
}
