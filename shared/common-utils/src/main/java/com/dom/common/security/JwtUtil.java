package com.dom.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT utility for token generation and validation.
 * Used by the API Gateway for authentication.
 */
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private static final String SECRET = "dom-secret-key-for-jwt-authentication-must-be-at-least-256-bits-long";
    private static final long EXPIRATION_MS = 3600000; // 1 hour
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private JwtUtil() {
    }

    /**
     * Generate a JWT token for a given user.
     */
    public static String generateToken(String userId, String role) {
        return Jwts.builder()
                .subject(userId)
                .claims(Map.of("role", role))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(KEY)
                .compact();
    }

    /**
     * Validate the token and extract the subject (userId).
     */
    public static String validateAndExtractUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if a token is valid (not expired, properly signed).
     */
    public static boolean isValid(String token) {
        return validateAndExtractUserId(token) != null;
    }
}
