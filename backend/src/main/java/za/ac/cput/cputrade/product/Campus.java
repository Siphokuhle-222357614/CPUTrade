package za.ac.cput.cputrade.product;

/**
 * Which CPUT campus a listing can be picked up/met at (US: multi-campus
 * filter) — drives the campus filter dropdown the same way {@link Category}
 * drives the category pills. CPUT's own published campus list; update this
 * enum if that ever changes rather than anywhere search/filter-related, since
 * every filter here is driven off it.
 */
public enum Campus {
    BELLVILLE,
    DISTRICT_SIX,
    GRANGER_BAY,
    MOWBRAY,
    WELLINGTON
}
