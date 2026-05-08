package com.parkar.parksaathi.controller.parking;

import com.parkar.parksaathi.dto.request.AddParkingRequest;
import com.parkar.parksaathi.dto.request.CreateParkingResponse;
import com.parkar.parksaathi.dto.response.ParkingSpotDetailResponse;
import com.parkar.parksaathi.model.Users;
import com.parkar.parksaathi.security.JwtService;
import com.parkar.parksaathi.service.parking.ParkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.parkar.parksaathi.constant.Constants.*;

@RestController
@RequestMapping(PARKING)
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;
    private final JwtService jwtService;

    @PostMapping(VERSION1 + CREATE_PARKING_ENDPOINT)
    public ResponseEntity<CreateParkingResponse> createSpot(
            @RequestBody AddParkingRequest request,
            @AuthenticationPrincipal Users currentUser) {
        CreateParkingResponse response = parkingService.addNewParking(request, currentUser.getId());
        return ResponseEntity.ok(response);
    }


    @GetMapping(VERSION1 + "/detail/{spotId}")
    public ResponseEntity<ParkingSpotDetailResponse> getSpotDetail(
            @PathVariable Long spotId) {
        try {
            ParkingSpotDetailResponse detailResponse = parkingService.getSpotDetail(spotId);
            return ResponseEntity.ok(detailResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(VERSION1 + NEARBY_PARKING_ENDPOINT)
    public ResponseEntity<List<ParkingSpotDetailResponse>> getNearbyParkingSpots(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "3.0") Double radiusKm) {
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

