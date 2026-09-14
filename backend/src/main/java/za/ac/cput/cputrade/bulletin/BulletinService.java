package za.ac.cput.cputrade.bulletin;

import za.ac.cput.cputrade.bulletin.dto.BulletinPostRequest;
import za.ac.cput.cputrade.bulletin.dto.BulletinPostResponse;
import za.ac.cput.cputrade.common.exception.ApiException;
import za.ac.cput.cputrade.user.Role;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BulletinService {

    private final BulletinPostRepository bulletinPostRepository;
    private final UserRepository userRepository;

    public BulletinService(BulletinPostRepository bulletinPostRepository, UserRepository userRepository) {
        this.bulletinPostRepository = bulletinPostRepository;
        this.userRepository = userRepository;
    }

    /** US5.2: browse the board — public, like the marketplace listing feed. Optional type filter for Lost & Found. */
    public List<BulletinPostResponse> listAll(BulletinType type) {
        List<BulletinPost> posts = type != null
                ? bulletinPostRepository.findByTypeOrderByCreatedAtDesc(type)
                : bulletinPostRepository.findAllByOrderByCreatedAtDesc();
        return posts.stream().map(BulletinPostResponse::from).toList();
    }

    /** US5.1: post a notice — any authenticated user. */
    @Transactional
    public BulletinPostResponse create(BulletinPostRequest request, Authentication auth) {
        User author = currentUser(auth);
        BulletinPost post = BulletinPost.builder()
                .author(author)
                .title(request.getTitle())
                .body(request.getBody())
                .type(request.getType() != null ? request.getType() : BulletinType.GENERAL)
                .build();
        return BulletinPostResponse.from(bulletinPostRepository.save(post));
    }

    /**
     * Toggle "resolved" (e.g. a lost item was reunited with its owner) — the
     * author or an admin. Purely cosmetic: it dims the post and lets the
     * board stay useful-looking instead of accumulating stale threads.
     */
    @Transactional
    public BulletinPostResponse setResolved(Long id, boolean resolved, Authentication auth) {
        BulletinPost post = bulletinPostRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Post not found"));
        User user = currentUser(auth);
        boolean isAuthor = post.getAuthor().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw ApiException.forbidden("You may only update your own posts");
        }
        post.setResolved(resolved);
        return BulletinPostResponse.from(bulletinPostRepository.save(post));
    }

    /** US5.3: remove a notice — the author or an admin. */
    @Transactional
    public void delete(Long id, Authentication auth) {
        BulletinPost post = bulletinPostRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Post not found"));
        User user = currentUser(auth);
        boolean isAuthor = post.getAuthor().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw ApiException.forbidden("You may only remove your own posts");
        }
        bulletinPostRepository.delete(post);
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> ApiException.unauthorized("Unknown user"));
    }
}
