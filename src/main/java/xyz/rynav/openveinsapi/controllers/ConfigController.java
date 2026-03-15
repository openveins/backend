package xyz.rynav.openveinsapi.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.rynav.openveinsapi.DTOs.Configs.ConfigPatchRequest;
import xyz.rynav.openveinsapi.DTOs.Configs.ConfigPatchResponse;
import xyz.rynav.openveinsapi.DTOs.Configs.PrivateConfigResponse;
import xyz.rynav.openveinsapi.DTOs.Configs.PublicAuthConfig;
import xyz.rynav.openveinsapi.interceptors.auth.AuthRequired;
import xyz.rynav.openveinsapi.services.ConfigService;


@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    @Autowired
    private final ConfigService configService;

    @GetMapping("/auth")
    public ResponseEntity<PublicAuthConfig> getAuthConfig() {
        return ResponseEntity.ok(configService.getPublicAuthConfig());
    }

    @AuthRequired
    @PatchMapping
    public ResponseEntity<ConfigPatchResponse> patchConfig(@Valid @RequestBody ConfigPatchRequest payload) {
        return ResponseEntity.ok(configService.patchConfig(payload));
    }

    @AuthRequired
    @GetMapping
    public ResponseEntity<PrivateConfigResponse> getConfig() {
        return ResponseEntity.ok(configService.getConfig());
    }
}
