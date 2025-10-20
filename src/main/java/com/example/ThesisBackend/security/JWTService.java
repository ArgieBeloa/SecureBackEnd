package com.example.ThesisBackend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JWTService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs}")
    private long jwtExpirationMs;

    /** ✅ Securely decode Base64 secret key */
    private SecretKey getSigningKey() {
        byte[] keyBytes;
        try {
            // Try Base64 decode first
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (IllegalArgumentException e) {
            // Fallback if secret isn’t Base64
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /** Generate JWT token */
    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .claims()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .add("role", role)
                .and()
                .signWith(getSigningKey())
                .compact();
    }

    /** Validate JWT */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            System.out.println("❌ Invalid JWT: " + e.getMessage());
            return false;
        }
    }

    /** Extract username (subject) */
    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            System.out.println("❌ Invalid token: " + e.getMessage());
            return null;
        }
    }

    /** Extract role */
    public String getRoleFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }
}
