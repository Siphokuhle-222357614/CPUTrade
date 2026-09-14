package za.ac.cput.cputrade.rating.dto;

/** A seller's aggregate rating — {@code average} is null when count is 0. */
public record RatingSummary(Double average, long count) {

    public static final RatingSummary NONE = new RatingSummary(null, 0);
}
