package xyz.rynav.openveinsapi.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.rynav.openveinsapi.models.OTPConfig;
import xyz.rynav.openveinsapi.models.User;
import xyz.rynav.openveinsapi.repositories.OTPRepository;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    private final OTPRepository otpRepository;

    public String generateToken(User user){
        OTPConfig config = otpRepository.findByUserId(user.getId()).orElse(null);
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("otpEnabled", config != null && config.isOtpEnabled());
        claims.put("role", "admin");
        return createToken(claims, user.getId());
    }

    public String generateOTPToken(String userId){
        Map<String, Object> claims = new HashMap<>();
        claims.put("verified", true);
        return createToken(claims, userId);
    }

    public String getSubject(String token){
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    private String createToken(Map<String, Object> claims, String subject){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSecretKey())
                .compact();

    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> jws = Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException ex){
            return false;
        }
    }

    private SecretKey getSecretKey(){
        byte[] secretBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(secretBytes);
    }
}
