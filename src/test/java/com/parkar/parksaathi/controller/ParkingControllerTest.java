package com.parkar.parksaathi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkar.parksaathi.controller.parking.ParkingController;
import com.parkar.parksaathi.dto.request.AddParkingRequest;
import com.parkar.parksaathi.dto.request.CreateParkingResponse;
import com.parkar.parksaathi.dto.response.ParkingSpotDetailResponse;
import com.parkar.parksaathi.dto.response.SpotInfo;
import com.parkar.parksaathi.dto.response.PricingInfo;
import com.parkar.parksaathi.dto.response.AvailabilityInfo;
import com.parkar.parksaathi.exception.customexceptions.InvalidLocationParametersException;
import com.parkar.parksaathi.exception.customexceptions.ResourceNotFoundException;
import com.parkar.parksaathi.exception.customexceptions.UnauthorizedException;
import com.parkar.parksaathi.model.Users;
import com.parkar.parksaathi.service.parking.ParkingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        mockUser.setId(1L);  // Ensure this is set
        mockUser.setEmail("test@example.com");

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
                .spotId(expectedSpotId)
                .message("Parking spot created successfully")
                .createdAt(LocalDateTime.now())
                .build();
        
        when(parkingService.addNewParking(any(AddParkingRequest.class), eq(1L)))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<CreateParkingResponse> response = parkingController.createSpot(request, mockUser);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedSpotId, response.getBody().getSpotId());
        assertEquals("Parking spot created successfully", response.getBody().getMessage());
        
        verify(parkingService, times(1)).addNewParking(request, 1L);
    }

    @Test
    void getSpotDetail_Success() {
        // Arrange
        Long spotId = 123L;
        ParkingSpotDetailResponse expectedResponse = ParkingSpotDetailResponse.builder()
                .spot(SpotInfo.builder()
                        .spotId("123")
                        .spotName("Test Parking")
                        .spotAddress("123 Test St")
                        .aboutSpot("Test Description")
                        .amenities(Arrays.asList("CCTV", "Security"))
                        .build())
                .pricing(PricingInfo.builder()
                        .hourly(10.0)
                        .daily(50.0)
                        .weekly(300.0)
                        .monthly(1000.0)
                        .build())
                .availability(AvailabilityInfo.builder()
                        .weekly(true)
                        .open24x7(false)
                        .build())
                .build();
        
        when(parkingService.getSpotDetail(spotId)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ParkingSpotDetailResponse> response = parkingController.getSpotDetail(spotId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("123", response.getBody().getSpot().getSpotId());
        assertEquals("Test Parking", response.getBody().getSpot().getSpotName());
        
        verify(parkingService, times(1)).getSpotDetail(spotId);
    }

    @Test
    void getSpotDetail_NotFound() {
        // Arrange
        Long spotId = 999L;
        when(parkingService.getSpotDetail(spotId))
                .thenThrow(new IllegalArgumentException("Parking spot not found"));

        // Act
        ResponseEntity<ParkingSpotDetailResponse> response = parkingController.getSpotDetail(spotId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getSpotDetail_InternalServerError() {
        // Arrange
        Long spotId = 123L;
        when(parkingService.getSpotDetail(spotId))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<ParkingSpotDetailResponse> response = parkingController.getSpotDetail(spotId);

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
        
        List<ParkingSpotDetailResponse> expectedSpots = Arrays.asList(
                createMockParkingSpotResponse("1", "Parking 1"),
                createMockParkingSpotResponse("2", "Parking 2")
        );
        
        when(parkingService.getNearbyParkingSpots(latitude, longitude, radiusKm))
                .thenReturn(expectedSpots);

        // Act
        ResponseEntity<List<ParkingSpotDetailResponse>> response = 
                parkingController.getNearbyParkingSpots(latitude, longitude, radiusKm);

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
        ResponseEntity<List<ParkingSpotDetailResponse>> response = 
                parkingController.getNearbyParkingSpots(latitude, longitude, radiusKm);

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
        ResponseEntity<List<ParkingSpotDetailResponse>> response = 
                parkingController.getNearbyParkingSpots(latitude, longitude, radiusKm);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    private ParkingSpotDetailResponse createMockParkingSpotResponse(String id, String name) {
        return ParkingSpotDetailResponse.builder()
                .spot(SpotInfo.builder()
                        .spotId(id)
                        .spotName(name)
                        .spotAddress("Test Address")
                        .aboutSpot("Test Description")
                        .amenities(Collections.emptyList())
                        .build())
                .pricing(PricingInfo.builder()
                        .hourly(10.0)
                        .daily(50.0)
                        .build())
                .availability(AvailabilityInfo.builder()
                        .weekly(true)
                        .open24x7(false)
                        .build())
                .build();
    }
}
