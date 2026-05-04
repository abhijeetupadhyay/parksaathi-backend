package com.parkar.parksaathi.controller.parking;

import com.parkar.parksaathi.dto.request.AddParkingRequest;
import com.parkar.parksaathi.service.parking.ParkingService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/v1/spots")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    // Inject JWT secret from configuration
    @Value("${jwt.secret}")
    private String jwtSecret;

    @PostMapping("/new")
    public ResponseEntity<Map<String, Long>> createSpot(
            @RequestHeader("x-parksaathi-accessToken") String token,
            @RequestBody AddParkingRequest request) {
        try {
            Long userId = 1L;//extractUserId(token);
            Long spotId = parkingService.addNewParking(request, userId);
            return ResponseEntity.ok(Collections.singletonMap("spotId", spotId));
        } catch (JwtException | IllegalArgumentException e) {
            // Invalid or expired token
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.emptyMap());
        }
    }

    /**
     * Extracts the user ID from the JWT token.
     * If AuthController provides a utility/service for this, use that instead.
     */
    /*public Long extractUserId(String token) {
        // Remove Bearer prefix if present
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (jwtSecret == null || jwtSecret.isEmpty()) {
            throw new IllegalStateException("JWT secret is not configured");
        }

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.claims(Map.of("key", key, "Token",  token));

        String subject = claims.getSubject();
        if (subject == null) {
            throw new IllegalArgumentException("JWT subject (userId) is missing");
        }
        return Long.parseLong(subject);
    }*/
}

