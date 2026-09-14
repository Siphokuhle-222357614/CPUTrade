package za.ac.cput.cputrade.notification;

/** What kind of in-app notification this is (US4.3) — drives no logic yet, just labels the row. */
public enum NotificationType {
    VENDOR_APPROVED,
    LISTING_REMOVED,
    NEW_MESSAGE,
    /** A listing on the recipient's wishlist just got cheaper. */
    PRICE_DROP,
    /** A listing on the recipient's wishlist was marked sold by someone else. */
    WISHLIST_ITEM_SOLD,
    /** A freshly-posted listing matches one of the recipient's saved search alerts. */
    SAVED_SEARCH_MATCH
}
