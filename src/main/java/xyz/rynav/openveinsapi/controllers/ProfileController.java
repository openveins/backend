package xyz.rynav.openveinsapi.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.rynav.openveinsapi.DTOs.Profile.TOTP.TOTPRequest;
import xyz.rynav.openveinsapi.DTOs.Profile.TOTP.TOTPVerifyRequest;
import xyz.rynav.openveinsapi.interceptors.auth.AuthRequired;
import xyz.rynav.openveinsapi.services.ProfileService;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PatchMapping("/2fa")
    @AuthRequired
    public ResponseEntity<?> enable2fa(@Valid @RequestBody TOTPRequest request, @Valid @CookieValue("auth_token") String token) throws Exception {
        return profileService.enable2fa(request, token);
    }

    @PostMapping("/2fa/verify")
    @AuthRequired
    public ResponseEntity<?> verify2fa(@Valid @RequestBody TOTPVerifyRequest request, @Valid @CookieValue("auth_token") String token) throws Exception {
        return profileService.verify2fa(request, token);
    }
}
