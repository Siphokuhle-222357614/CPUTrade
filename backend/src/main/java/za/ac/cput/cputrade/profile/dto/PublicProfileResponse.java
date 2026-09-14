package za.ac.cput.cputrade.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import za.ac.cput.cputrade.user.Role;

import java.time.LocalDateTime;

/** What another user sees when they open someone's profile — never includes contact info like email. */
@Getter
@Builder
@AllArgsConstructor
public class PublicProfileResponse {
    private Long id;
    private String username;
    private String campusHandle;
    private Role role;
    private LocalDateTime memberSince;
    private Double ratingAverage;
    private long ratingCount;
    private long activeListingCount;
    private boolean online;
    private LocalDateTime lastActiveAt;
}
