package com.parkar.parksaathi.controller.parking;

import com.parkar.parksaathi.dto.request.AddParkingRequest;
import com.parkar.parksaathi.security.JwtService;
import com.parkar.parksaathi.service.parking.ParkingService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/v1/spots")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;
    private final JwtService jwtService;

    @PostMapping("/new")
    public ResponseEntity<Map<String, Long>> createSpot(
            @RequestHeader("x-parksaathi-accessToken") String token,
            @RequestBody AddParkingRequest request) {
        try {
            Long userId = jwtService.extractUserId(token);
            Long spotId = parkingService.addNewParking(request, userId);
            return ResponseEntity.ok(Collections.singletonMap("spotId", spotId));
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.emptyMap());
        }
    }

}

