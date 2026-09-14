package za.ac.cput.cputrade.trust;

/**
 * The single source of truth for what "Verified Seller" means, so the
 * threshold is never duplicated (and never drifts) between the marketplace
 * listing responses and a user's public profile.
 */
public final class SellerTrust {

    /** Completed sales at or above this earn the "Verified Seller" badge — a trust signal buyers can act on. */
    public static final int VERIFIED_SELLER_THRESHOLD = 3;

    private SellerTrust() {
    }

    public static boolean isVerified(long completedSalesCount) {
        return completedSalesCount >= VERIFIED_SELLER_THRESHOLD;
    }
}
