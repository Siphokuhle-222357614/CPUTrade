package za.ac.cput.cputrade.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import za.ac.cput.cputrade.user.AccountStatus;
import za.ac.cput.cputrade.user.Role;
import za.ac.cput.cputrade.user.User;

import java.time.LocalDateTime;

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
    private AccountStatus accountStatus;
    private String suspensionReason;
    private LocalDateTime suspendedUntil;

    public static UserSummaryDto from(User user) {
        return UserSummaryDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .campusHandle(user.getCampusHandle())
                .role(user.getRole())
                .verified(user.isVerified())
                .vendorApproved(user.isVendorApproved())
                .accountStatus(user.getAccountStatus())
                .suspensionReason(user.getSuspensionReason())
                .suspendedUntil(user.getSuspendedUntil())
                .build();
    }
}
