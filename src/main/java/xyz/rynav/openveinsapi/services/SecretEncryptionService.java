package xyz.rynav.openveinsapi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SecretEncryptionService {

    private static final String ALG = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    @Value("${TOTP_ENCRYPTIONKEY}")
    private String ENCRYPTION_KEY;

    private SecretKey getEncryptionKey() {
        byte[] keyBytes = Base64.getDecoder().decode(ENCRYPTION_KEY);
        return new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String text) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALG);
        cipher.init(Cipher.ENCRYPT_MODE, getEncryptionKey(), new GCMParameterSpec(TAG_LENGTH, iv));

        byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));

        byte[] combined =  new byte[IV_LENGTH + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
        System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public String decrypt(String encrypted) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encrypted);

        byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
        byte[] encryptedBytes = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

        Cipher cipher = Cipher.getInstance(ALG);
        cipher.init(Cipher.DECRYPT_MODE, getEncryptionKey(), new GCMParameterSpec(TAG_LENGTH, iv));

        return new String(cipher.doFinal(encryptedBytes), StandardCharsets.UTF_8);
    }
}
