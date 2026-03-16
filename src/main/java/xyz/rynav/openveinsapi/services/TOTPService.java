package xyz.rynav.openveinsapi.services;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base32;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import xyz.rynav.openveinsapi.exceptions.Auth.AuthException;
import xyz.rynav.openveinsapi.models.OTPConfig;
import xyz.rynav.openveinsapi.models.User;
import xyz.rynav.openveinsapi.models.UserSettings.Settings;
import xyz.rynav.openveinsapi.models.UserSettings.UserSettings;
import xyz.rynav.openveinsapi.repositories.OTPRepository;
import xyz.rynav.openveinsapi.repositories.UserSettingsRepository;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class TOTPService {

    private final TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator();
    private final OTPRepository otpRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final SecretEncryptionService secretEncryptionService;
    private final UserSettingsRepository userSettingsRepository;

    private static final long OTP_TTL = 90;

    public String setupTOTP(String userId, String userEmail) throws Exception {
        OTPConfig otpConfig = otpRepository.findByUserId(userId).orElse(null);
        String base32Key;

        if(otpConfig == null) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(totp.getAlgorithm());
            keyGenerator.init(160);
            SecretKey secretKey = keyGenerator.generateKey();

            base32Key = new Base32().encodeToString(secretKey.getEncoded());
            String secretEncrypted = secretEncryptionService.encrypt(base32Key);

            OTPConfig newConfig = new OTPConfig();
            newConfig.setUserId(userId);
            newConfig.setOtpSecret(secretEncrypted);
            newConfig.setOtpEnabled(false);
            newConfig.setOtpVerified(false);
            newConfig.setOtpVerificationPending(true);
            otpRepository.save(newConfig);

        }else if(otpConfig.isOtpVerificationPending()) {
            base32Key = secretEncryptionService.decrypt(otpConfig.getOtpSecret());
        }else {
            throw new AuthException("OTP is already enrolled.");
        }

        return generateTotpString(userEmail, base32Key);
    }

    public boolean initialVerification(User user, String code) throws Exception {
        OTPConfig otpConfig = otpRepository.findByUserId(user.getId()).orElseThrow(() -> new Exception("User has no OTP setup."));
        UserSettings userSettings = userSettingsRepository.findByUserId(user.getId()).orElseThrow(() -> new Exception("User has no user settings."));
        Settings settings = userSettings.getSettings();

        if(otpConfig.isOtpEnabled() && otpConfig.isOtpVerified()) {
            settings.setOtpEnabled(true);
            userSettingsRepository.save(userSettings);
            throw new AuthException("OTP has already been verified.");
        }

        if(!verifyCode(user.getId(), Integer.parseInt(code))){
            throw new AuthException("OTP failed to verify!");
        }

        otpConfig.setOtpEnabled(true);
        otpConfig.setOtpVerified(true);
        otpConfig.setOtpVerificationPending(false);
        otpRepository.save(otpConfig);

        settings.setOtpEnabled(true);
        userSettingsRepository.save(userSettings);

        return true;
    }

    public String generateTotpString(String email, String secret) {
        return "otpauth://totp/Openveins:" + email
                + "?secret=" + secret
                + "&issuer=Openveins"
                + "&algorithm=SHA1"
                + "&digits=6"
                + "&period=30";
    }

    public boolean verifyCode(String userId, int otp) throws Exception {
        OTPConfig otpConfig = otpRepository.findByUserId(userId).orElseThrow(() -> new Exception("User has no OTP setup."));

        byte[] secretBytes = new Base32().decode(secretEncryptionService.decrypt(otpConfig.getOtpSecret()));
        SecretKey secretKey = new SecretKeySpec(secretBytes, totp.getAlgorithm());

        Instant now = Instant.now();
        long currentWindow = now.getEpochSecond() / 30;

        for (int i = -1; i <= 1; i++){
            long windowIndex = currentWindow + i;
            Instant window = Instant.ofEpochSecond(windowIndex * 30);
            int expected = totp.generateOneTimePassword(secretKey, window);

            if(expected == otp) {
                if(isCodeAlreadyUsed(userId, otp,  windowIndex)) {
                    throw new AuthException("OTP code has already been used.");
                }
                return true;
            }
        }
        return false;
    }

    private boolean isCodeAlreadyUsed(String userId, int otp, long windowIndex ){
        String key = "used_otp:" + userId + ":" + windowIndex + ":" + otp;
        Boolean isNew = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", OTP_TTL, TimeUnit.SECONDS);
        return !isNew;
    }

}
