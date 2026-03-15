package xyz.rynav.openveinsapi.services;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.rynav.openveinsapi.DTOs.Auth.AuthResponse;
import xyz.rynav.openveinsapi.DTOs.Auth.LoginRequest;
import xyz.rynav.openveinsapi.DTOs.Auth.OTPRequest;
import xyz.rynav.openveinsapi.DTOs.Auth.RegisterRequest;
import xyz.rynav.openveinsapi.exceptions.Auth.AuthException;
import xyz.rynav.openveinsapi.models.OTPConfig;
import xyz.rynav.openveinsapi.models.User;
import xyz.rynav.openveinsapi.repositories.OTPRepository;
import xyz.rynav.openveinsapi.repositories.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CloudflareTurnstileService cloudflareTurnstileService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OTPRepository otpRepository;
    private final JwtService jwtService;
    private final TOTPService totpService;

    @Transactional
    public ResponseEntity<AuthResponse> login(LoginRequest request, HttpServletResponse response) throws Exception {

        if(!cloudflareTurnstileService.verifyCaptchaToken(request.getCaptcha())){
            throw new AuthException("Invalid Captcha Token");
        }

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AuthException("Invalid email or password"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            TimeUnit.SECONDS.sleep(2);
            throw new AuthException("Invalid email or password");
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
            AuthResponse finalResponse = AuthResponse.builder().totpRequired(false).message("Successfully logged in!").build();
            return ResponseEntity.ok(finalResponse);
        }

        token = jwtService.generateOTPToken(user.getId());
        ResponseCookie cookie = ResponseCookie.from("totp_token", token)
                .httpOnly(true)
                .sameSite("Strict")
                .maxAge(300)
                .path("/")
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        AuthResponse finalResponse = AuthResponse.builder().totpRequired(false).message("TOTP verification is required!").build();
        return ResponseEntity.ok(finalResponse);
    }

    public ResponseEntity<?> login2fa(OTPRequest request, HttpServletResponse response, String totpToken) throws Exception {
        if(totpToken == null || totpToken.isEmpty() || !jwtService.validateToken(totpToken)){
            throw new AuthException("Invalid token");
        }

        if(request.getCode().isBlank()){
            throw new AuthException("Code is required");
        }

        String subject = jwtService.getSubject(totpToken);
        Optional<User> user =  userRepository.findById(subject);

        if(user.isEmpty()){
            throw new AuthException("User not found");
        }

        int code;
        try{
            code = Integer.parseInt(request.getCode());
        } catch(NumberFormatException e){
            throw new AuthException("Invalid code");
        }

        boolean verified = totpService.verifyCode(subject, code);

        if(!verified){
            throw new AuthException("Invalid code");
        }

        String token = jwtService.generateToken(user.get());

        ResponseCookie cookie = ResponseCookie.from("auth_token", token)
                .httpOnly(true)
                .sameSite("Strict")
                .maxAge(86400)
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
        return ResponseEntity.ok(Map.of("message", "Successfully logged in!"));
    }

    public ResponseEntity<?> register(RegisterRequest request, HttpServletResponse response) throws Exception {
        if(!cloudflareTurnstileService.verifyCaptchaToken(request.getCaptcha())){
            throw new AuthException("Invalid Captcha Token");
        }

        if(userRepository.existsByEmail(request.getEmail())){
            throw new AuthException("Email already exists");
        }
        if(userRepository.existsByUsername(request.getUsername())){
            throw new AuthException("Username already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        ResponseCookie cookie = ResponseCookie.from("auth_token", token)
                .httpOnly(true)
                .sameSite("Strict")
                .maxAge(86400)
                .path("/")
                .secure(true)
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(Map.of("message", "Successfully signed up!"));

    }

    public ResponseEntity<?> me(String token){
        if(token == null || token.isEmpty() || !jwtService.validateToken(token)){
            throw new AuthException("Invalid token");
        }

        String subject = jwtService.getSubject(token);

        Optional<User> user = userRepository.findById(subject);

        if(user.isEmpty()){
            throw new AuthException("User not found?");
        }

        return ResponseEntity.ok(Map.of("username", user.get().getUsername(), "id",  user.get().getId(), "email", user.get().getEmail()));
    }

}
