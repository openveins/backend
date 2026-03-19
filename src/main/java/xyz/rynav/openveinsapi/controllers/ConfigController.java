package xyz.rynav.openveinsapi.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.rynav.openveinsapi.DTOs.Configs.ConfigPatchRequest;
import xyz.rynav.openveinsapi.interceptors.auth.AuthRequired;
import xyz.rynav.openveinsapi.services.ConfigService;


@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    @Autowired
    private final ConfigService configService;

    @GetMapping("/auth")
    public ResponseEntity<?> getAuthConfig() {
        return configService.getPublicAuthConfig();
    }

    @AuthRequired
    @PatchMapping
    public ResponseEntity<?> patchConfig(@Valid @RequestBody ConfigPatchRequest payload) {
        return configService.patchConfig(payload);
    }

    @AuthRequired
    @GetMapping
    public ResponseEntity<?> getConfig() {
        return configService.getConfig();
    }
}
