package za.ac.cput.cputrade.trust;

/** Why a listing or user was reported — drives no logic, just gives the admin queue a quick filterable label. */
public enum ReportReason {
    SCAM,
    INAPPROPRIATE,
    SPAM,
    HARASSMENT,
    OTHER
}
