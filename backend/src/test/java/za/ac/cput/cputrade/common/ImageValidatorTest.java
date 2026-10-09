package za.ac.cput.cputrade.common;

import za.ac.cput.cputrade.common.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class ImageValidatorTest {

    private static String b64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] withHeader(int size, int... header) {
        byte[] data = new byte[size];
        for (int i = 0; i < header.length; i++) data[i] = (byte) header[i];
        return data;
    }

    @Test
    void acceptsAJpeg() {
        assertEquals(100, ImageValidator.validate(b64(withHeader(100, 0xFF, 0xD8))).length);
    }

    @Test
    void acceptsAPngEvenWithADataUrlPrefix() {
        String dataUrl = "data:image/png;base64," + b64(withHeader(100, 0x89, 0x50, 0x4E, 0x47));
        assertDoesNotThrow(() -> ImageValidator.validate(dataUrl));
    }

    @Test
    void rejectsAnImageOver4Mb() {
        String big = b64(withHeader(ImageValidator.MAX_IMAGE_BYTES + 1, 0xFF, 0xD8));
        assertThrows(ApiException.class, () -> ImageValidator.validate(big));
    }

    @Test
    void rejectsAFileThatIsNotJpegOrPngWhateverItIsCalled() {
        String exe = b64(withHeader(100, 'M', 'Z'));
        assertThrows(ApiException.class, () -> ImageValidator.validate(exe));
    }

    @Test
    void rejectsInvalidBase64() {
        assertThrows(ApiException.class, () -> ImageValidator.validate("%%% not base64 %%%"));
    }
}
