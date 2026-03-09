package xyz.rynav.openveinsapi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.rynav.openveinsapi.DTOs.Auth.AuthResponse;
import xyz.rynav.openveinsapi.DTOs.Auth.LoginRequest;
import xyz.rynav.openveinsapi.DTOs.Auth.OTPRequest;
import xyz.rynav.openveinsapi.DTOs.Auth.RegisterRequest;
import xyz.rynav.openveinsapi.exceptions.Auth.AuthException;
import xyz.rynav.openveinsapi.models.User;
import xyz.rynav.openveinsapi.repositories.OTPRepository;
import xyz.rynav.openveinsapi.repositories.UserRepository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OTPRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudflareTurnstileService cloudflareTurnstileService;
    private final JwtService jwtService;
    private final TOTPService TOTPService;

    private final static Logger logger = Logger.getLogger(AuthService.class.getName());

    @Transactional
    public AuthResponse register(RegisterRequest request) throws Exception {
        if (!cloudflareTurnstileService.verifyCaptchaToken(request.getCaptcha())) {
            throw new AuthException("Captcha Token is invalid!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email is already registered");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthException("Username already taken");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return AuthResponse.builder().token(token).message("Account created successfully.").build();
    }

    public AuthResponse login(LoginRequest request) throws Exception {

        logger.info(request.toString());

        if(!cloudflareTurnstileService.verifyCaptchaToken(request.getCaptcha())) {
            throw new AuthException("Captcha Token is invalid!");
        }

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AuthException("Invalid email or password"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            TimeUnit.SECONDS.sleep(3);
            throw new AuthException("Invalid email or password");
        }

        if(otpRepository.findByUserId(user.getId()).isEmpty() || !otpRepository.findByUserId(user.getId()).get().isOtpEnabled()) {
            String token = jwtService.generateToken(user);
            return AuthResponse.builder().token(token).message("Successfully logged in.").otpRequired(false).build();
        }

        String token = jwtService.generateOTPToken(user.getId());
        return AuthResponse.builder().token(token).message("OTP Required.").otpRequired(true).build();

    }

    public AuthResponse verify2fa(OTPRequest request) throws Exception {
        String token = request.getToken();
        if(token == null || !jwtService.validateToken(token)) {
            throw new AuthException("Invalid token");
        }

        if(request.getCode().isBlank()){
            throw new AuthException("Code is required");
        }

        String subject = jwtService.getSubject(token);


        Optional<User> user = userRepository.findById(subject);

        if(user.isEmpty()) {
            throw new AuthException("User not found");
        }

        boolean verified = TOTPService.verifyCode(subject, Integer.parseInt(request.getCode()));

        if(!verified) {
            throw new AuthException("Invalid code");
        }

        String responseToken = jwtService.generateToken(user.get());

        return AuthResponse.builder().token(responseToken).message("Successfully logged in.").otpRequired(false).build();
    }

//    public AuthResponse me(Request request) {
//        String header = request.getHeader("Authorization");
//
//        if(header == null || !header.startsWith("Bearer ")) {
//            throw new AuthException("Authorization header is invalid!");
//        }
//        String  token = header.substring(7);
//        logger.info(token);
//        return AuthResponse.builder().token(token).message("Successfully logged in.").build();
//    }


//    public AuthResponse me(@RequestHeader String authHeader) {
//
//    }
}
