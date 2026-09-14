package za.ac.cput.cputrade.user;

/**
 * A user's role on the platform.
 *
 * <p>There is no self-registration path to {@link #ADMIN} — the first admin
 * account must be seeded directly in the database.
 */
public enum Role {
    STUDENT,
    VENDOR,
    ADMIN
}
