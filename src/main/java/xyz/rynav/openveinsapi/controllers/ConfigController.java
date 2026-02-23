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
        PublicAuthConfig response = configService.getPublicAuthConfig();
        return ResponseEntity.ok(response);
    }

    @AuthRequired
    @PatchMapping
    public ResponseEntity<ConfigPatchResponse> patchConfig(@Valid @RequestBody ConfigPatchRequest payload) {
        ConfigPatchResponse response = configService.patchConfig(payload);

        return ResponseEntity.ok(response);
    }

    @AuthRequired
    @GetMapping
    public ResponseEntity<PrivateConfigResponse> getConfig() {
        PrivateConfigResponse response = configService.getConfig();
        return ResponseEntity.ok(response);
    }
}
