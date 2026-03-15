package xyz.rynav.openveinsapi.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.rynav.openveinsapi.DTOs.Auth.LoginRequest;
import xyz.rynav.openveinsapi.DTOs.Auth.OTPRequest;
import xyz.rynav.openveinsapi.DTOs.Auth.RegisterRequest;
import xyz.rynav.openveinsapi.interceptors.auth.AuthRequired;
import xyz.rynav.openveinsapi.services.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) throws Exception {
        return authService.login(request, response);
    }

    @PostMapping("/login/2fa")
    public ResponseEntity<?> login2fa(@Valid @RequestBody OTPRequest request, HttpServletResponse response, @Valid @CookieValue("totp_token") String totpToken) throws Exception {
        return authService.login2fa(request, response, totpToken);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) throws Exception {
        return authService.register(request, response);
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("auth_token", "")
                .httpOnly(true)
                .sameSite("Strict")
                .secure(true)
                .maxAge(0)
                .path("/")
                .build();

        response.setHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok(Map.of("status", "OK"));
    }

    @GetMapping("/me")
    @AuthRequired
    public ResponseEntity<?> me(@Valid @CookieValue("auth_token") String token) {
        return authService.me(token);
    }
}
