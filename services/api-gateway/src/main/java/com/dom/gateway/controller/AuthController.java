package com.dom.gateway.controller;

import com.dom.common.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Authentication controller for JWT token generation.
 * Provides login endpoint that issues JWT tokens.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    // Simulated user store (in production, this would be a database)
    private static final Map<String, String> USERS = Map.of(
            "admin", "admin123",
            "user1", "pass123",
            "user2", "pass123",
            "user3", "pass123"
    );

    /**
     * POST /api/auth/login - Authenticate and receive JWT token.
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        log.info("Login attempt for user: {}", username);

        if (username == null || password == null) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Username and password are required"
            )));
        }

        String storedPassword = USERS.get(username);
        if (storedPassword == null || !storedPassword.equals(password)) {
            log.warn("Failed login attempt for user: {}", username);
            return Mono.just(ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "Invalid credentials"
            )));
        }

        String role = username.equals("admin") ? "ADMIN" : "USER";
        String token = JwtUtil.generateToken(username, role);

        log.info("User {} authenticated successfully with role {}", username, role);
        return Mono.just(ResponseEntity.ok(Map.of(
                "success", true,
                "token", token,
                "username", username,
                "role", role,
                "expiresIn", 3600
        )));
    }
}
