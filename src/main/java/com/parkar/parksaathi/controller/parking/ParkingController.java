package com.parkar.parksaathi.controller.parking;

import com.parkar.parksaathi.dto.request.AddParkingRequest;
import com.parkar.parksaathi.service.parking.ParkingService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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

    @PostMapping("/new")
    public ResponseEntity<Map<String, Long>> createSpot(
            @RequestHeader("x-parksaathi-accessToken") String token,
            @RequestBody AddParkingRequest request) {
        
        // logic to get userId from token omitted for brevity
        Long userId = extractUserId(token); 
        
        Long spotId = parkingService.addNewParking(request, userId);
        
        return ResponseEntity.ok(Collections.singletonMap("spotId", spotId));
    }

        public Long extractUserId(String token) {
            // Handle Bearer prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Assuming userId is stored in the "sub" (subject) field
            // or a custom "userId" claim
            return Long.parseLong(claims.getSubject());
        }

}