package com.keyscout24.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Port of get_secret_key() from app/__init__.py.
 * Reads instance/&lt;name&gt;.txt, or creates it with 64 random bytes (hex) if missing or empty.
 */
@Component
public class SecretKeyProvider {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final Path instancePath;

    public SecretKeyProvider(@Value("${keyscout.instance-path}") String instancePath) {
        this.instancePath = Path.of(instancePath);
    }

    public synchronized String get(String secretName) {
        try {
            Files.createDirectories(instancePath);
            Path secretFile = instancePath.resolve(secretName + ".txt");

            if (Files.exists(secretFile)) {
                String existing = Files.readString(secretFile, StandardCharsets.UTF_8).strip();
                if (!existing.isEmpty()) {
                    return existing;
                }
            }

            byte[] bytes = new byte[64];
            RANDOM.nextBytes(bytes);
            String secret = HexFormat.of().formatHex(bytes);
            Files.writeString(secretFile, secret, StandardCharsets.UTF_8);
            return secret;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read or create secret " + secretName, e);
        }
    }

    public String giftCodeSecret() {
        return get("gift_code_secret");
    }

    public String botUsername() {
        return get("support_username").substring(0, 16);
    }

    public String botPassword() {
        return get("support_password").substring(0, 20);
    }
}
