package za.ac.cput.cputrade.admin;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminActionHistoryService {

    private final AdminActionRepository repository;

    public AdminActionHistoryService(AdminActionRepository repository) {
        this.repository = repository;
    }

    public void record(Authentication actor, AdminActionType action, String targetType,
                       Long targetId, String targetLabel, String details) {
        repository.save(new AdminAction(
                action,
                actor.getName(),
                targetType,
                targetId,
                targetLabel,
                details
        ));
    }

    public List<AdminActionResponse> latest() {
        return repository.findTop100ByOrderByCreatedAtDescIdDesc().stream()
                .map(AdminActionResponse::from)
                .toList();
    }
}
