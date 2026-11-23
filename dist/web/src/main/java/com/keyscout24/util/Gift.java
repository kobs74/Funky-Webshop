package com.keyscout24.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.keyscout24.config.SecretKeyProvider;

/**
 * Port of util/gift.py. A gift code is a signed token "header.payload.signature"
 * (base64url without padding, HMAC-SHA256), then hex encoded, reversed and prefixed with "GC".
 */
@Component
public class Gift {

    private static final String WRAPPING_PROTOCOL = "EXTREME-SECURE";
    private static final String DELIVERY_METHOD = "P4RT-Y";
    private static final String BOX_PREFIX = "GC";
    private static final String SEPARATOR = ".";

    /** json.dumps({...}, separators=(',', ':'), sort_keys=True) of the fixed header. */
    private static final String HEADER_JSON =
            "{\"gift_code_creation_method\":\"" + DELIVERY_METHOD + "\",\"gift_code_elf\":\"" + WRAPPING_PROTOCOL + "\"}";

    private final SecretKeyProvider secrets;

    public Gift(SecretKeyProvider secrets) {
        this.secrets = secrets;
    }

    public String create(String gift) {
        String wrappedHeader = wrap(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
        String wrappedGift = wrap(gift.getBytes(StandardCharsets.UTF_8));
        String token = wrappedHeader + SEPARATOR + wrappedGift;
        return token + SEPARATOR + wrap(seal(token));
    }

    public String read(String giftCode) {
        String[] parts = giftCode.split("\\.", -1);
        if (parts.length != 3) {
            throw new GiftCodeException();
        }
        String wrappedHeader = parts[0];
        String wrappedGift = parts[1];
        String wrappedSeal = parts[2];

        String header;
        try {
            header = new String(unwrap(wrappedHeader), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new GiftCodeException(e);
        }
        if (!header.contains("\"gift_code_elf\":\"" + WRAPPING_PROTOCOL + "\"")
                || !header.contains("\"gift_code_creation_method\":\"" + DELIVERY_METHOD + "\"")) {
            throw new GiftCodeException();
        }

        String expectedSeal = wrap(seal(wrappedHeader + SEPARATOR + wrappedGift));
        if (!MessageDigest.isEqual(wrappedSeal.getBytes(StandardCharsets.UTF_8),
                expectedSeal.getBytes(StandardCharsets.UTF_8))) {
            throw new GiftCodeException();
        }

        try {
            return new String(unwrap(wrappedGift), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new GiftCodeException(e);
        }
    }

    public String pack(String giftCode) {
        String hex = HexFormat.of().formatHex(giftCode.getBytes(StandardCharsets.UTF_8));
        return BOX_PREFIX + new StringBuilder(hex).reverse();
    }

    public String unpack(String giftCode) {
        if (!giftCode.startsWith(BOX_PREFIX)) {
            throw new GiftCodeException();
        }
        String reversed = new StringBuilder(giftCode.substring(BOX_PREFIX.length())).reverse().toString();
        try {
            return new String(HexFormat.of().parseHex(reversed), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new GiftCodeException(e);
        }
    }

    private byte[] seal(String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secrets.giftCodeSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String wrap(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private static byte[] unwrap(String data) {
        return Base64.getUrlDecoder().decode(data);
    }
}
