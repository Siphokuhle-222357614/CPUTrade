package za.ac.cput.cputrade.trust;

import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.trust.dto.AppealRequest;
import za.ac.cput.cputrade.trust.dto.AppealResponse;
import za.ac.cput.cputrade.user.AccountStatus;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;
import za.ac.cput.cputrade.user.notification.EmailNotifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppealService {

    private final AppealRepository appealRepository;
    private final UserRepository userRepository;
    private final EmailNotifier emailNotifier;

    public AppealService(AppealRepository appealRepository, UserRepository userRepository, EmailNotifier emailNotifier) {
        this.appealRepository = appealRepository;
        this.userRepository = userRepository;
        this.emailNotifier = emailNotifier;
    }

    @Transactional
    public AppealResponse submit(AppealRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> ApiException.notFound("No account with that username"));
        if (user.getAccountStatus() != AccountStatus.SUSPENDED) {
            throw ApiException.badRequest("This account is not currently suspended");
        }
        Appeal appeal = Appeal.builder().user(user).message(request.getMessage()).build();
        return AppealResponse.from(appealRepository.save(appeal));
    }

    public List<AppealResponse> list(AppealStatus status) {
        List<Appeal> appeals = status != null
                ? appealRepository.findByStatusOrderByCreatedAtDesc(status)
                : appealRepository.findAllByOrderByCreatedAtDesc();
        return appeals.stream().map(AppealResponse::from).toList();
    }

    @Transactional
    public AppealResponse approve(Long id) {
        Appeal appeal = findOrThrow(id);
        appeal.setStatus(AppealStatus.APPROVED);
        appeal.setResolvedAt(LocalDateTime.now());

        User user = appeal.getUser();
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setSuspendedUntil(null);
        user.setSuspensionReason(null);
        userRepository.save(user);
        emailNotifier.sendAccountReactivatedEmail(user);

        return AppealResponse.from(appealRepository.save(appeal));
    }

    @Transactional
    public AppealResponse reject(Long id) {
        Appeal appeal = findOrThrow(id);
        appeal.setStatus(AppealStatus.REJECTED);
        appeal.setResolvedAt(LocalDateTime.now());
        return AppealResponse.from(appealRepository.save(appeal));
    }

    private Appeal findOrThrow(Long id) {
        return appealRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Appeal not found"));
    }
}
