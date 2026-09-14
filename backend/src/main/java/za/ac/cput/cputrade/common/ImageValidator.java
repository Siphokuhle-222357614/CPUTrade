package za.ac.cput.cputrade.common;

import za.ac.cput.cputrade.common.exception.ApiException;

import java.util.Base64;

/**
 * Validates a Base64-encoded product image (US2.1: JPEG/PNG only, max
 * 500KB). Base64 inflates size by ~33%, so the cap is enforced on the
 * <em>decoded</em> byte length, never the raw string length.
 */
public final class ImageValidator {

    public static final int MAX_IMAGE_BYTES = 500 * 1024; // 500KB

    private ImageValidator() {
    }

    /** @return the decoded image bytes, once validated. */
    public static byte[] validate(String base64Image) {
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(stripDataUrlPrefix(base64Image));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Image is not valid Base64 data");
        }

        if (decoded.length > MAX_IMAGE_BYTES) {
            throw ApiException.badRequest("Image exceeds the 500KB limit");
        }

        if (!isJpegOrPng(decoded)) {
            throw ApiException.badRequest("Only JPEG or PNG images are allowed");
        }

        return decoded;
    }

    private static String stripDataUrlPrefix(String value) {
        int commaIndex = value.indexOf(',');
        return value.startsWith("data:") && commaIndex >= 0 ? value.substring(commaIndex + 1) : value;
    }

    private static boolean isJpegOrPng(byte[] bytes) {
        if (bytes.length < 4) return false;
        boolean isJpeg = (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8;
        boolean isPng = (bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47;
        return isJpeg || isPng;
    }
}
