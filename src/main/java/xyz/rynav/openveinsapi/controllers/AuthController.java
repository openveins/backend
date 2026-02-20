package xyz.rynav.openveinsapi.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.rynav.openveinsapi.DTOs.Auth.AuthResponse;
import xyz.rynav.openveinsapi.DTOs.Auth.LoginRequest;
import xyz.rynav.openveinsapi.DTOs.Auth.RegisterRequest;
import xyz.rynav.openveinsapi.services.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) throws InterruptedException {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request){
        AuthResponse response = authService.register(request);

        return ResponseEntity.ok(response);
    }
}
