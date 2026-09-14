package za.ac.cput.cputrade.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import za.ac.cput.cputrade.user.Role;
import za.ac.cput.cputrade.user.User;

/**
 * Safe, public-facing view of a {@link User} — never includes the password
 * hash. Always map through this (or {@link #from}) instead of returning the
 * entity directly.
 */
@Getter
@Builder
@AllArgsConstructor
public class UserSummaryDto {
    private Long id;
    private String username;
    private String campusHandle;
    private Role role;
    private boolean verified;
    private boolean vendorApproved;

    public static UserSummaryDto from(User user) {
        return UserSummaryDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .campusHandle(user.getCampusHandle())
                .role(user.getRole())
                .verified(user.isVerified())
                .vendorApproved(user.isVendorApproved())
                .build();
    }
}
