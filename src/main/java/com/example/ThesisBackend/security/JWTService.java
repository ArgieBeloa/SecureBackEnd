package com.example.ThesisBackend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JWTService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        // Must be at least 256 bits (32+ chars)
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /** Generate JWT token for a given username */
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

    /** Validate token signature & expiration */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()                     // ✅ correct for 0.12.5
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            System.out.println("❌ Invalid JWT: " + e.getMessage());
            return false;
        }
    }

    /** Extract username (subject) from token */
    public String getUsernameFromToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("Empty JWT token");
            }
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

    public String getRoleFromToken(String token) {
        return Jwts.parser() // ✅ same parser
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class); // ✅ safely extract the “role” claim
    }
}
