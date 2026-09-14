package za.ac.cput.cputrade.bulletin;

import jakarta.validation.Valid;
import za.ac.cput.cputrade.bulletin.dto.BulletinPostRequest;
import za.ac.cput.cputrade.bulletin.dto.BulletinPostResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** US5.1–US5.3 — campus bulletin board. GET is public; POST/DELETE require auth (SecurityConfig). */
@RestController
@RequestMapping("/api/bulletin")
public class BulletinController {

    private final BulletinService bulletinService;

    public BulletinController(BulletinService bulletinService) {
        this.bulletinService = bulletinService;
    }

    @GetMapping
    public ResponseEntity<List<BulletinPostResponse>> list(@RequestParam(required = false) BulletinType type) {
        return ResponseEntity.ok(bulletinService.listAll(type));
    }

    @PostMapping
    public ResponseEntity<BulletinPostResponse> create(@Valid @RequestBody BulletinPostRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bulletinService.create(request, auth));
    }

    @PatchMapping("/{id}/resolved")
    public ResponseEntity<BulletinPostResponse> setResolved(
            @PathVariable Long id, @RequestParam boolean resolved, Authentication auth
    ) {
        return ResponseEntity.ok(bulletinService.setResolved(id, resolved, auth));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        bulletinService.delete(id, auth);
        return ResponseEntity.noContent().build();
    }
}
