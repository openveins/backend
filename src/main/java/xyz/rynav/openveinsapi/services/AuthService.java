package xyz.rynav.openveinsapi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.rynav.openveinsapi.DTOs.Auth.AuthResponse;
import xyz.rynav.openveinsapi.DTOs.Auth.LoginRequest;
import xyz.rynav.openveinsapi.DTOs.Auth.RegisterRequest;
import xyz.rynav.openveinsapi.exceptions.auth.AuthException;
import xyz.rynav.openveinsapi.models.User;
import xyz.rynav.openveinsapi.repositories.UserRepository;

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
        if (!cloudflareTurnstileService.verifyCaptchaToken(request.getCaptchaToken())) {
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

    public AuthResponse login(LoginRequest request){

        logger.info(request.toString());

        if(!cloudflareTurnstileService.verifyCaptchaToken(request.getCaptchaToken())) {
            throw new AuthException("Captcha Token is invalid!");
        }

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AuthException("Invalid email or password"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new AuthException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return AuthResponse.builder().token(token).message("Successfully logged in.").build();
    }

}
