package xyz.rynav.openveinsapi.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.rynav.openveinsapi.models.User;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    public String generateToken(User user){
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("role", "admin");
        return createToken(claims, user.getId());
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
            Jws<Claims> jws = Jwts.parser().verifyWith((SecretKey) getSecretKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException ex){
            return false;
        }
    }

    private Key getSecretKey(){
        byte[] secretBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(secretBytes);
    }
}
