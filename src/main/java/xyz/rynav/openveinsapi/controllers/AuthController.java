package xyz.rynav.openveinsapi.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.rynav.openveinsapi.DTOs.Auth.AuthResponse;
import xyz.rynav.openveinsapi.DTOs.Auth.LoginRequest;
import xyz.rynav.openveinsapi.DTOs.Auth.OTPRequest;
import xyz.rynav.openveinsapi.DTOs.Auth.RegisterRequest;
import xyz.rynav.openveinsapi.services.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) throws Exception {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/2fa")
    public ResponseEntity<AuthResponse> login2fa(@Valid @RequestBody OTPRequest request) throws Exception {
        AuthResponse response = authService.verify2fa(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) throws Exception {
        AuthResponse response = authService.register(request);

        return ResponseEntity.ok(response);
    }

//    @GetMapping("/me")
//    @AuthRequired
//    public ResponseEntity<AuthResponse> me(@RequestHeader("Autorization") String authheader) {
//
//    }
}
