package com.nivasafinance.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Digest/hash utilities (e.g. SHA-256 for file hashing, deduplication).
 */
public final class DigestUtils {

    private static final String SHA_256 = "SHA-256";

    private DigestUtils() {
    }

    /**
     * Returns the SHA-256 digest of the input as a lowercase hex string.
     *
     * @param input bytes to hash (may be empty, not null)
     * @return 64-character hex string
     */
    public static String sha256Hex(byte[] input) {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hash = digest.digest(input);
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(SHA_256 + " not available", e);
        }
    }
}
