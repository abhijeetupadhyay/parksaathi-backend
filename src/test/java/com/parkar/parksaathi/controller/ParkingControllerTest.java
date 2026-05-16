package com.parkar.parksaathi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkar.parksaathi.controller.parking.ParkingController;
import com.parkar.parksaathi.dto.request.AddParkingRequest;
import com.parkar.parksaathi.dto.request.AvailabilityDto;
import com.parkar.parksaathi.dto.request.CreateParkingResponse;
import com.parkar.parksaathi.dto.response.ParkingDetailsResponse;
import com.parkar.parksaathi.dto.response.ParkingInfo;
import com.parkar.parksaathi.dto.response.PricingAndCapacityInfo;
import com.parkar.parksaathi.enums.UserStatus;
import com.parkar.parksaathi.model.Users;
import com.parkar.parksaathi.service.parking.ParkingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingControllerTest {

    @Mock
    private ParkingService parkingService;

    @InjectMocks
    private ParkingController parkingController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Users mockUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(parkingController).build();
        objectMapper = new ObjectMapper();

        // Setup mock user with ID = 1L
        mockUser = new Users();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setStatus(UserStatus.ACTIVE);

        // Setup security context
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(mockUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


    @Test
    void createSpot_Success() {
        // Arrange
        AddParkingRequest request = new AddParkingRequest();
        Long expectedSpotId = 123L;

        CreateParkingResponse expectedResponse = CreateParkingResponse.builder()
                .parkingId(expectedSpotId)
                .createdAt(LocalDateTime.now())
                .build();

        when(parkingService.addNewParking(any(AddParkingRequest.class), any(Users.class)))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<CreateParkingResponse> response = parkingController.createParking(request, mockUser);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedSpotId, response.getBody().getParkingId());

        verify(parkingService, times(1)).addNewParking(request, mockUser);
    }

    @Test
    void getParkingDetail_Success() {
        // Arrange
        Long spotId = 123L;
        ParkingDetailsResponse expectedResponse = ParkingDetailsResponse.builder()
                .parkingInfo(ParkingInfo.builder()
                        .id(123L)
                        .name("Test Parking")
                        .address("123 Test St")
                        .description("Test Description")
                        .amenities(new HashSet<>(Arrays.asList("CCTV", "Security")))
                        .build())
                .pricingAndCapacityInfo(PricingAndCapacityInfo.builder()
                        .vehicleConfigs(Collections.emptySet())
                        .build())
                .availabilityInfo(new AvailabilityDto())
                .build();

        when(parkingService.getParkingDetail(spotId)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ParkingDetailsResponse> response = parkingController.getParkingDetail(spotId, mockUser);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(123L, response.getBody().getParkingInfo().getId());
        assertEquals("Test Parking", response.getBody().getParkingInfo().getName());

        verify(parkingService, times(1)).getParkingDetail(spotId);
    }

    @Test
    void getParkingDetail_NotFound() {
        // Arrange
        Long spotId = 999L;
        when(parkingService.getParkingDetail(spotId))
                .thenThrow(new IllegalArgumentException("Parking spot not found"));

        // Act
        ResponseEntity<ParkingDetailsResponse> response = parkingController.getParkingDetail(spotId, mockUser);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getParkingDetail_InternalServerError() {
        // Arrange
        Long spotId = 123L;
        when(parkingService.getParkingDetail(spotId))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<ParkingDetailsResponse> response = parkingController.getParkingDetail(spotId, mockUser);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getNearbyParkingSpots_Success() {
        // Arrange
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double radiusKm = 5.0;

        List<ParkingDetailsResponse> expectedSpots = Arrays.asList(
                createMockParkingSpotResponse("1", "Parking 1"),
                createMockParkingSpotResponse("2", "Parking 2")
        );

        when(parkingService.getNearbyParkingSpots(latitude, longitude, radiusKm))
                .thenReturn(expectedSpots);

        // Act
        ResponseEntity<List<ParkingDetailsResponse>> response =
                parkingController.getNearbyParkingSpots(latitude, longitude, radiusKm, mockUser);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(parkingService, times(1)).getNearbyParkingSpots(latitude, longitude, radiusKm);
    }

    @Test
    void getNearbyParkingSpots_BadRequest() {
        // Arrange
        Double latitude = null;
        Double longitude = -74.0060;
        Double radiusKm = 5.0;

        when(parkingService.getNearbyParkingSpots(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid parameters"));

        // Act
        ResponseEntity<List<ParkingDetailsResponse>> response =
                parkingController.getNearbyParkingSpots(latitude, longitude, radiusKm, mockUser);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getNearbyParkingSpots_InternalServerError() {
        // Arrange
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double radiusKm = 5.0;

        when(parkingService.getNearbyParkingSpots(latitude, longitude, radiusKm))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act
        ResponseEntity<List<ParkingDetailsResponse>> response =
                parkingController.getNearbyParkingSpots(latitude, longitude, radiusKm, mockUser);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    private ParkingDetailsResponse createMockParkingSpotResponse(String id, String name) {
        return ParkingDetailsResponse.builder()
                .parkingInfo(ParkingInfo.builder()
                        .id(Long.valueOf(id))
                        .name(name)
                        .address("Test Address")
                        .description("Test Description")
                        .amenities(Collections.emptySet())
                        .build())
                .pricingAndCapacityInfo(PricingAndCapacityInfo.builder()
                        .vehicleConfigs(Collections.emptySet())
                        .build())
                .availabilityInfo(new AvailabilityDto())
                .build();
    }
}
