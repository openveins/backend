package xyz.rynav.openveinsapi.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.rynav.openveinsapi.DTOs.Configs.ConfigPatchRequest;
import xyz.rynav.openveinsapi.DTOs.Configs.ConfigPatchResponse;
import xyz.rynav.openveinsapi.DTOs.Configs.PublicAuthConfig;
import xyz.rynav.openveinsapi.services.ConfigService;

import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    @Autowired
    private final ConfigService configService;
    private final Logger logger = Logger.getLogger(ConfigController.class.getName());

    @GetMapping("/auth")
    public ResponseEntity<PublicAuthConfig> getAuthConfig() {
        PublicAuthConfig response = configService.getPublicAuthConfig();
        return ResponseEntity.ok(response);
    }

    // TODO: ADD AUTH!!!!!!
    // THIS IS JUST THE BAREBONES FUNCTION TO SEE IF IT WORKS.
    // ADD AUTH ASAP.
    @PatchMapping
    public ResponseEntity<ConfigPatchResponse> patchConfig(@Valid @RequestBody ConfigPatchRequest payload) {
        ConfigPatchResponse response = configService.patchConfig(payload);

        return ResponseEntity.ok(response);
    }
}
