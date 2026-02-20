package xyz.rynav.openveinsapi.services;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.Request;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.rynav.openveinsapi.DTOs.Auth.AuthResponse;
import xyz.rynav.openveinsapi.DTOs.Auth.LoginRequest;
import xyz.rynav.openveinsapi.DTOs.Auth.RegisterRequest;
import xyz.rynav.openveinsapi.exceptions.Auth.AuthException;
import xyz.rynav.openveinsapi.models.User;
import xyz.rynav.openveinsapi.repositories.UserRepository;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudflareTurnstileService cloudflareTurnstileService;
    private final JwtService jwtService;

    private final static Logger logger = Logger.getLogger(AuthResponse.class.getName());

    @Transactional
    public AuthResponse register(RegisterRequest request) {
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

    public AuthResponse login(LoginRequest request) throws InterruptedException {

        logger.info(request.toString());

        if(!cloudflareTurnstileService.verifyCaptchaToken(request.getCaptcha())) {
            throw new AuthException("Captcha Token is invalid!");
        }

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AuthException("Invalid email or password"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            TimeUnit.SECONDS.sleep(3);
            throw new AuthException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return AuthResponse.builder().token(token).message("Successfully logged in.").build();
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
}
