package xyz.rynav.openveinsapi.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import xyz.rynav.openveinsapi.DTOs.Profile.TOTP.TOTPEnableResponse;
import xyz.rynav.openveinsapi.DTOs.Profile.TOTP.TOTPRequest;
import xyz.rynav.openveinsapi.DTOs.Profile.TOTP.TOTPVerifyRequest;
import xyz.rynav.openveinsapi.DTOs.Profile.TOTP.TOTPVerifyResponse;
import xyz.rynav.openveinsapi.exceptions.Auth.AuthException;
import xyz.rynav.openveinsapi.models.OTPConfig;
import xyz.rynav.openveinsapi.models.User;
import xyz.rynav.openveinsapi.repositories.OTPRepository;
import xyz.rynav.openveinsapi.repositories.UserRepository;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final OTPRepository otpRepository;
    private final TOTPService TOTPService;
    private final JwtService jwtService;

    @Transactional
    public TOTPEnableResponse enable2fa(@Valid @RequestBody TOTPRequest request, @RequestHeader("Authorization") String authHeader) throws Exception {
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtService.getSubject(token);

        User user = userRepository.findById(userId).orElseThrow(() -> new Exception("Invalid Token."));
        Optional<OTPConfig> config = otpRepository.findByUserId(user.getId());

        if(!request.isEnable()){
            config.ifPresent(c -> otpRepository.deleteByUserId(user.getId()));
            return TOTPEnableResponse.builder()
                    .status("success")
                    .message("2FA disabled")
                    .success(true)
                    .data(Map.of("token", jwtService.generateToken(user)))
                    .build();
        }

        if(config.isPresent() && config.get().isOtpVerified()){
            return TOTPEnableResponse.builder().status("no_change").message("2FA is already enabled!").success(true).build();
        }

        String qrCode = TOTPService.setupTOTP(user.getId(), user.getEmail());
        return TOTPEnableResponse.builder()
                .status("verify")
                .data(Map.of("qrURI", Base64.getEncoder().encodeToString(qrCode.getBytes(StandardCharsets.UTF_8))))
                .success(true)
                .build();

    }

    @Transactional
    public TOTPVerifyResponse verify2fa(@Valid @RequestBody TOTPVerifyRequest request, @RequestHeader("Authorization") String authHeader) throws Exception {
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtService.getSubject(token);

        User user = userRepository.findById(userId).orElseThrow(() -> new Exception("Invalid Token."));

        boolean response = TOTPService.initialVerification(user, request.getCode());

        if(response){
            return TOTPVerifyResponse.builder()
                    .message("Successfully enabled 2FA!")
                    .data(Map.of("token", jwtService.generateToken(user)))
                    .success(true)
                    .build();
        }

        throw new AuthException("Failed to validate the TOTP code.");
    }
}
