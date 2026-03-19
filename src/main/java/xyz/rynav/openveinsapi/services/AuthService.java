package xyz.rynav.openveinsapi.services;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.rynav.openveinsapi.DTOs.ApiResponse;
import xyz.rynav.openveinsapi.DTOs.Auth.*;
import xyz.rynav.openveinsapi.exceptions.Auth.AuthException;
import xyz.rynav.openveinsapi.models.OTPConfig;
import xyz.rynav.openveinsapi.models.User;
import xyz.rynav.openveinsapi.models.UserSettings.UserSettings;
import xyz.rynav.openveinsapi.repositories.OTPRepository;
import xyz.rynav.openveinsapi.repositories.UserRepository;
import xyz.rynav.openveinsapi.repositories.UserSettingsRepository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CloudflareTurnstileService cloudflareTurnstileService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OTPRepository otpRepository;
    private final JwtService jwtService;
    private final TOTPService totpService;
    private final UserSettingsRepository userSettingsRepository;

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Transactional
    public ResponseEntity<ApiResponse<AuthResponse>> login(LoginRequest request, HttpServletResponse response) throws Exception {

        if(!cloudflareTurnstileService.verifyCaptchaToken(request.getCaptcha())){
            throw new AuthException("Captcha verification failed");
        }

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if(user == null){
            TimeUnit.SECONDS.sleep(2);
            throw new AuthException("Invalid username or password");
        }

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            TimeUnit.SECONDS.sleep(2);
            throw new AuthException("Invalid username or password");
        }

        String token;
        Optional<OTPConfig> otpConfig = otpRepository.findByUserId(user.getId());
        if(otpConfig.isEmpty() || !otpConfig.get().isOtpEnabled()) {
            token = jwtService.generateToken(user);
            ResponseCookie cookie = ResponseCookie.from("auth_token", token)
                    .httpOnly(true)
                    .sameSite("Strict")
                    .maxAge(86400)
                    .path("/")
                    .secure(true)
                    .build();

            response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            return ResponseEntity.ok(ApiResponse.ok("Successfully logged in!", new AuthResponse(false)));
        }

        token = jwtService.generateOTPToken(user.getId());
        ResponseCookie cookie = ResponseCookie.from("totp_token", token)
                .httpOnly(true)
                .sameSite("Strict")
                .maxAge(300)
                .path("/")
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(ApiResponse.ok("TOTP verification is required!!", new AuthResponse(true)));
    }

    public ResponseEntity<ApiResponse<AuthResponse>> login2fa(OTPRequest request, HttpServletResponse response, String totpToken) throws Exception {
        if(totpToken == null || totpToken.isEmpty() || !jwtService.validateToken(totpToken)){
            throw new AuthException("Invalid TOTP token");
        }

        if(request.getCode().isBlank()){
            throw new AuthException("Code is required");
        }

        String subject = jwtService.getSubject(totpToken);
        User user =  userRepository.findById(subject).orElse(null);

        if(user == null){
            throw new Exception("User not found.");
        }

        int code;
        try{
            code = Integer.parseInt(request.getCode());
        } catch(NumberFormatException e){
            throw new AuthException("Invalid code format");
        }

        boolean verified = totpService.verifyCode(subject, code);

        if(!verified){
            throw new AuthException("Provided code is invalid!");
        }

        String token = jwtService.generateToken(user);

        ResponseCookie cookie = ResponseCookie.from("auth_token", token)
                .httpOnly(true)
                .sameSite("Strict")
                .maxAge(86400)
                .path("/")
                .secure(true)
                .build();

        ResponseCookie clearTotp = ResponseCookie.from("totp_token", "")
                .httpOnly(true)
                .sameSite("Strict")
                .secure(true)
                .maxAge(0)
                .path("/")
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearTotp.toString());
        return ResponseEntity.ok(ApiResponse.ok("Successfully logged in!", null));
    }

    @Transactional
    public ResponseEntity<ApiResponse<?>> register(RegisterRequest request, HttpServletResponse response) throws Exception {
        if(!cloudflareTurnstileService.verifyCaptchaToken(request.getCaptcha())){
            throw new AuthException("Invalid Captcha Token");
        }

        if(userRepository.existsByEmail(request.getEmail())){
            throw new AuthException("Email is used already");
        }
        if(userRepository.existsByUsername(request.getUsername())){
            throw new AuthException("Username is used already");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();


        userRepository.save(user);

        UserSettings userSettings = UserSettings.builder().userId(user.getId()).build();

        userSettingsRepository.save(userSettings);

        String token = jwtService.generateToken(user);

        ResponseCookie cookie = ResponseCookie.from("auth_token", token)
                .httpOnly(true)
                .sameSite("Strict")
                .maxAge(86400)
                .path("/")
                .secure(true)
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(ApiResponse.ok("Successfully registered!", null));

    }

    @Transactional
    public ResponseEntity<ApiResponse<MeResponse>> me(String token){
        if(token == null || token.isEmpty() || !jwtService.validateToken(token)){
            throw new AuthException("Invalid token");
        }

        String subject = jwtService.getSubject(token);

        User user = userRepository.findById(subject).orElse(null);

        if(user == null) {
            throw new AuthException("User not found?");
        }

        UserSettings settings = userSettingsRepository.findByUserId(user.getId()).orElseThrow(() -> new AuthException("User not found"));

        return ResponseEntity.ok(
                ApiResponse.ok("Success", new MeResponse(user.getId(), user.getUsername(), user.getEmail(), settings.getSettings()))
        );
    }

}
