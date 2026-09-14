package za.ac.cput.cputrade.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PresenceResponse {
    private boolean online;
    private LocalDateTime lastActiveAt;
}
