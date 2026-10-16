package za.ac.cput.cputrade.user;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.ac.cput.cputrade.storage.ImageStorageService;
import za.ac.cput.cputrade.user.dto.DeleteAccountRequest;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountLifecycleService accountLifecycleService;
    private final ImageStorageService imageStorageService;

    public AccountController(AccountLifecycleService accountLifecycleService,
                             ImageStorageService imageStorageService) {
        this.accountLifecycleService = accountLifecycleService;
        this.imageStorageService = imageStorageService;
    }

    @DeleteMapping
    public ResponseEntity<Void> anonymizeAccount(@Valid @RequestBody DeleteAccountRequest request,
                                                  Authentication authentication) {
        accountLifecycleService.anonymizeAccount(authentication.getName(), request.getPassword())
                .forEach(imageStorageService::delete);
        return ResponseEntity.noContent().build();
    }
}