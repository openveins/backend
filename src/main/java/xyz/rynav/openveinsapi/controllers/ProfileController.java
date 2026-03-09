package xyz.rynav.openveinsapi.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.rynav.openveinsapi.DTOs.Profile.TOTP.TOTPEnableResponse;
import xyz.rynav.openveinsapi.DTOs.Profile.TOTP.TOTPRequest;
import xyz.rynav.openveinsapi.DTOs.Profile.TOTP.TOTPVerifyRequest;
import xyz.rynav.openveinsapi.DTOs.Profile.TOTP.TOTPVerifyResponse;
import xyz.rynav.openveinsapi.interceptors.auth.AuthRequired;
import xyz.rynav.openveinsapi.services.ProfileService;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PatchMapping("/2fa")
    @AuthRequired
    public ResponseEntity<TOTPEnableResponse> enable2fa(@Valid @RequestBody TOTPRequest request, @RequestHeader("Authorization") String token) throws Exception {
        final TOTPEnableResponse response = profileService.enable2fa(request, token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/2fa/verify")
    @AuthRequired
    public ResponseEntity<TOTPVerifyResponse> verify2fa(@Valid @RequestBody TOTPVerifyRequest request, @RequestHeader("Authorization") String authHeader) throws Exception {
        final TOTPVerifyResponse response = profileService.verify2fa(request, authHeader);

        return ResponseEntity.ok(response);
    }
}
