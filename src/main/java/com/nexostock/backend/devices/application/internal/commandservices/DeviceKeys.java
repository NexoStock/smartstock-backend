package com.nexostock.backend.devices.application.internal.commandservices;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Generates device keys and compares them by their hash.
 * Genera claves de dispositivo y las compara por su hash.
 */
final class DeviceKeys {

    private static final SecureRandom RANDOM = new SecureRandom();

    private DeviceKeys() {
    }

    /** 32 random bytes as 64 hexadecimal characters. */
    static String newKey() {
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        return HexFormat.of().formatHex(raw);
    }

    static String sha256(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Constant-time comparison, so timing does not reveal the hash. */
    static boolean matches(String plainKey, String storedHash) {
        if (plainKey == null || storedHash == null) {
            return false;
        }
        return MessageDigest.isEqual(
                sha256(plainKey).getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8));
    }
}
