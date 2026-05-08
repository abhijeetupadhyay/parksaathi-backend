package com.parkar.parksaathi.controller.parking;

import com.parkar.parksaathi.dto.request.AddParkingRequest;
import com.parkar.parksaathi.dto.response.ParkingSpotDetailResponse;
import com.parkar.parksaathi.security.JwtService;
import com.parkar.parksaathi.service.parking.ParkingService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.parkar.parksaathi.constant.Constants.*;

@RestController
@RequestMapping(PARKING)
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;
    private final JwtService jwtService;

    @PostMapping(VERSION1 + CREATE_PARKING_ENDPOINT)
    public ResponseEntity<Map<String, Long>> createSpot(
          //  @RequestHeader("x-parksaathi-accessToken") String token,
            @RequestBody AddParkingRequest request) {
        try {
            Long userId = 2L;//jwtService.extractUserId(token);
            Long spotId = parkingService.addNewParking(request, userId);
            return ResponseEntity.ok(Collections.singletonMap("spotId", spotId));
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.emptyMap());
        }
    }

    @GetMapping(VERSION1 + "/detail/{spotId}")
    public ResponseEntity<ParkingSpotDetailResponse> getSpotDetail(
            @PathVariable Long spotId,
            @RequestHeader("x-parksaathi-device") String device,
            @RequestHeader("x-parksaathi-correlation-id") String correlationId,
            @RequestHeader("x-parksaathi-version") String version) {
        try {
            ParkingSpotDetailResponse detailResponse = parkingService.getSpotDetail(spotId);
            return ResponseEntity.ok(detailResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(VERSION1 + "/nearby")
    public ResponseEntity<List<ParkingSpotDetailResponse>> getNearbyParkingSpots(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm,
            @RequestHeader("x-parksaathi-device") String device,
            @RequestHeader("x-parksaathi-correlation-id") String correlationId,
            @RequestHeader("x-parksaathi-version") String version) {
        try {
            List<ParkingSpotDetailResponse> nearbySpots = parkingService.getNearbyParkingSpots(
                    latitude, longitude, radiusKm);
            return ResponseEntity.ok(nearbySpots);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}

