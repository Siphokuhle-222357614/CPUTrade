package za.ac.cput.cputrade.admin;

import java.time.LocalDateTime;

public record AdminActionResponse(
        Long id,
        AdminActionType action,
        String actorUsername,
        String targetType,
        Long targetId,
        String targetLabel,
        String details,
        LocalDateTime createdAt
) {
    public static AdminActionResponse from(AdminAction entry) {
        return new AdminActionResponse(
                entry.getId(),
                entry.getAction(),
                entry.getActorUsername(),
                entry.getTargetType(),
                entry.getTargetId(),
                entry.getTargetLabel(),
                entry.getDetails(),
                entry.getCreatedAt()
        );
    }
}
