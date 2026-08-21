package com.eventx.util;

import lombok.extern.slf4j.Slf4j;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Hex;

@Slf4j
public class SignatureUtils {

    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

    public static boolean verifyRazorpaySignature(String data, String signature, String secret) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(secretKeySpec);

            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = Hex.encodeHexString(hmacBytes);

            return MessageDigest.isEqual(expectedSignature.getBytes(), signature.getBytes());
        } catch (Exception e) {
            log.error("Error verifying Razorpay signature", e);
            return false;
        }
    }
}
