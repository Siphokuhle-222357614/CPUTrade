package za.ac.cput.cputrade.bulletin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import za.ac.cput.cputrade.bulletin.BulletinPost;
import za.ac.cput.cputrade.bulletin.BulletinType;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BulletinPostResponse {
    private Long id;
    private Long authorId;
    private String authorUsername;
    private String title;
    private String body;
    private BulletinType type;
    private boolean resolved;
    private LocalDateTime createdAt;

    public static BulletinPostResponse from(BulletinPost post) {
        return BulletinPostResponse.builder()
                .id(post.getId())
                .authorId(post.getAuthor().getId())
                .authorUsername(post.getAuthor().getUsername())
                .title(post.getTitle())
                .body(post.getBody())
                .type(post.getType())
                .resolved(post.isResolved())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
