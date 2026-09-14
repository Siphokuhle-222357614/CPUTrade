package za.ac.cput.cputrade.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import za.ac.cput.cputrade.common.ImageValidator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Stores product photos as real files on disk instead of Base64 text in the
 * database — the DB no longer carries image bytes on every row, and every
 * {@code GET /api/products} response is a fraction of its previous size
 * since it now returns a short URL instead of an inline blob.
 *
 * <p>Files live under {@code app.upload-dir}/products and are served back by
 * {@code WebConfig}'s {@code /uploads/**} static mapping. On an ephemeral
 * filesystem host (see DEPLOYMENT.md), swap this out for a real object-store
 * implementation (S3, Cloudinary, ...) behind the same two methods —
 * everything else in the codebase only ever calls {@code store}/{@code delete}.
 */
@Slf4j
@Service
public class ImageStorageService {

    private final Path uploadRoot;

    public ImageStorageService(@Value("${app.upload-dir:./uploads}") String uploadDir) {
        this.uploadRoot = Path.of(uploadDir, "products");
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create upload directory: " + uploadRoot, e);
        }
    }

    /** Validates, decodes, and writes the image; returns the absolute URL to fetch it back. */
    public String store(String base64Image) {
        byte[] decoded = ImageValidator.validate(base64Image); // throws on invalid/oversized/wrong-type
        String extension = ImageValidator.isPng(decoded) ? "png" : "jpg";
        String filename = UUID.randomUUID() + "." + extension;

        try {
            Files.write(uploadRoot.resolve(filename), decoded);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save uploaded image", e);
        }

        // Absolute URL against whichever origin is handling this request —
        // correct in dev (backend on a different port than the frontend) and
        // in prod, without needing a separately-configured public base URL.
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/products/")
                .path(filename)
                .toUriString();
    }

    /** Best-effort cleanup — a missing/unparseable URL is logged, never thrown, since it's not the caller's fault. */
    public void delete(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
        try {
            Files.deleteIfExists(uploadRoot.resolve(filename));
        } catch (IOException e) {
            log.warn("Could not delete image file for URL {}", imageUrl, e);
        }
    }
}
