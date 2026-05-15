package com.parkar.parksaathi.service;

import com.parkar.parksaathi.dto.request.*;
import com.parkar.parksaathi.dto.response.ParkingSpotDetailResponse;
import com.parkar.parksaathi.enums.DayOfWeekEnum;
import com.parkar.parksaathi.exception.customexceptions.InvalidLocationParametersException;
import com.parkar.parksaathi.exception.customexceptions.ResourceNotFoundException;
import com.parkar.parksaathi.model.*;
import com.parkar.parksaathi.repository.AmenityRepository;
import com.parkar.parksaathi.repository.ParkingRepository;
import com.parkar.parksaathi.service.parking.ParkingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @Mock
    private ParkingRepository parkingRepo;

    @Mock
    private AmenityRepository facilityRepo;

    @InjectMocks
    private ParkingService parkingService;

    private AddParkingRequest validRequest;
    private Parking mockParking;

    @BeforeEach
    void setUp() {
        // Setup valid request
        validRequest = new AddParkingRequest();
        validRequest.setDescription("Test Parking");
        validRequest.setEmergencyContact("123-456-7890");

        AddressDto addressDto = new AddressDto();
        addressDto.setAddressLine1("123 Test St");
        addressDto.setCity("Test City");
        addressDto.setLatitude(BigDecimal.valueOf(40.7128));
        addressDto.setLongitude(BigDecimal.valueOf(-74.0060));
        validRequest.setAddress(addressDto);

        AvailabilityDto availabilityDto = new AvailabilityDto();
        availabilityDto.setOpen24Hours(false);
        availabilityDto.setStartTime(LocalTime.of(8, 0));
        availabilityDto.setEndTime(LocalTime.of(20, 0));
        availabilityDto.setAdStartDate(LocalDate.from(LocalDateTime.now()));
        availabilityDto.setAdEndDate(LocalDate.from(LocalDateTime.now().plusMonths(1)));
        availabilityDto.setDays(Arrays.asList(DayOfWeekEnum.WED));
        validRequest.setAvailability(availabilityDto);

        validRequest.setAmenities(Arrays.asList("CCTV", "Security"));

        VehicleConfigDto vehicleConfig = new VehicleConfigDto();
        vehicleConfig.setVehicleTypeId(1);
        vehicleConfig.setMaxCapacity(10);
        vehicleConfig.setHourlyRate(BigDecimal.valueOf(10));
        vehicleConfig.setDailyRate(BigDecimal.valueOf(50));
        vehicleConfig.setWeeklyRate(BigDecimal.valueOf(300));
        vehicleConfig.setMonthlyRate(BigDecimal.valueOf(1000));
        validRequest.setVehicleConfigs(Arrays.asList(vehicleConfig));

        // Setup mock parking listing
        mockParking = createMockParkingListing();
    }

    @Test
    void addNewParking_Success() {
        // Arrange
        Long userId = 1L;
        Long expectedSpotId = 123L;

        Set<Amenity> mockFacilities = new HashSet<>();
        mockFacilities.add(createFacility("CCTV"));
        mockFacilities.add(createFacility("Security"));

        when(facilityRepo.findByAmenityNameIn(anyList())).thenReturn(mockFacilities);
        when(parkingRepo.save(any(Parking.class))).thenAnswer(invocation -> {
            Parking saved = invocation.getArgument(0);
            saved.setId(expectedSpotId);
            return saved;
        });

        // Act
        CreateParkingResponse response = parkingService.addNewParking(validRequest, userId);

        // Assert
        assertNotNull(response);
        assertEquals(expectedSpotId, response.getParkingId());
        assertEquals("Parking spot created successfully", response.getMessage());
        assertNotNull(response.getCreatedAt());

        // Verify repository interactions
        verify(facilityRepo, times(1)).findByAmenityNameIn(validRequest.getAmenities());
        verify(parkingRepo, times(1)).save(any(Parking.class));

        // Verify saved entity
        ArgumentCaptor<Parking> captor = ArgumentCaptor.forClass(Parking.class);
        verify(parkingRepo).save(captor.capture());
        Parking savedListing = captor.getValue();

        assertEquals(userId, savedListing.getOwner().getId());
        assertEquals(validRequest.getDescription(), savedListing.getDescription());
        assertEquals(validRequest.getEmergencyContact(), savedListing.getEmergencyContact());
        assertEquals(validRequest.getAddress().getAddressLine1(), savedListing.getAddress().getAddressLine1());
        assertEquals(validRequest.getAddress().getCity(), savedListing.getAddress().getCity());
        assertEquals(validRequest.getAddress().getLatitude(), savedListing.getAddress().getLatitude());
        assertEquals(validRequest.getAddress().getLongitude(), savedListing.getAddress().getLongitude());
        assertFalse(savedListing.getIsOpen24Hours());
        assertEquals(1, savedListing.getVehicleConfigs().size());
    }

    @Test
    void addNewParking_WithNullFacilities() {
        // Arrange
        validRequest.setAmenities(null);
        Long userId = 1L;

        when(parkingRepo.save(any(Parking.class))).thenAnswer(invocation -> {
            Parking saved = invocation.getArgument(0);
            saved.setId(123L);
            return saved;
        });

        // Act
        CreateParkingResponse response = parkingService.addNewParking(validRequest, userId);

        // Assert
        assertNotNull(response);
        assertEquals(123L, response.getParkingId());

        // Verify facilityRepo was not called with null amenities
        verify(facilityRepo, never()).findByAmenityNameIn(anyList());
    }

    @Test
    void getParkingDetail_Success() {
        // Arrange
        Long spotId = 123L;
        when(parkingRepo.findById(spotId)).thenReturn(Optional.of(mockParking));

        // Act
        ParkingSpotDetailResponse response = parkingService.getParkingDetail(spotId);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getParkingInfo());
        assertEquals("123", response.getParkingInfo().getParkingId());
        assertEquals("Test Parking", response.getParkingInfo().getParkingName());
        assertEquals("123 Test St, Test City", response.getParkingInfo().getParkingAddress());

        assertNotNull(response.getPricingInfo());
        assertEquals(10.0, response.getPricingInfo().getHourly());
        assertEquals(50.0, response.getPricingInfo().getDaily());

        assertNotNull(response.getAvailabilityInfo());

        verify(parkingRepo, times(1)).findById(spotId);
    }

    @Test
    void getParkingDetail_NotFound() {
        // Arrange
        Long spotId = 999L;
        when(parkingRepo.findById(spotId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> parkingService.getParkingDetail(spotId)
        );

        assertEquals("Parking spot not found with id: 999", exception.getMessage());
        verify(parkingRepo, times(1)).findById(spotId);
    }

    @Test
    void getParkingDetail_WithEmptyVehicleConfigs() {
        // Arrange
        Long spotId = 123L;
        mockParking.setVehicleConfigs(new HashSet<>());
        when(parkingRepo.findById(spotId)).thenReturn(Optional.of(mockParking));

        // Act
        ParkingSpotDetailResponse response = parkingService.getParkingDetail(spotId);

        // Assert
        assertNotNull(response);
        assertEquals(0.0, response.getPricingInfo().getHourly());
        assertEquals(0.0, response.getPricingInfo().getDaily());
        assertEquals(0.0, response.getPricingInfo().getWeekly());
        assertEquals(0.0, response.getPricingInfo().getMonthly());
    }

    @Test
    void getNearbyParkingSpots_Success() {
        // Arrange
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double radiusKm = 5.0;

        List<Parking> nearbyListings = Arrays.asList(
                mockParking,
                createMockParkingListing()
        );

        when(parkingRepo.findNearbyParkingSpots(latitude, longitude, radiusKm))
                .thenReturn(nearbyListings);

        // Act
        List<ParkingSpotDetailResponse> response =
                parkingService.getNearbyParkingSpots(latitude, longitude, radiusKm);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());

        verify(parkingRepo, times(1)).findNearbyParkingSpots(latitude, longitude, radiusKm);
    }

    @Test
    void getNearbyParkingSpots_InvalidLatitude() {
        // Act & Assert
        InvalidLocationParametersException exception = assertThrows(
                InvalidLocationParametersException.class,
                () -> parkingService.getNearbyParkingSpots(null, -74.0060, 5.0)
        );

        assertEquals("Invalid location parameters: latitude, longitude and radiusKm must be provided", exception.getMessage());
        verify(parkingRepo, never()).findNearbyParkingSpots(any(), any(), any());
    }

    @Test
    void getNearbyParkingSpots_InvalidLongitude() {
        // Act & Assert
        InvalidLocationParametersException exception = assertThrows(
                InvalidLocationParametersException.class,
                () -> parkingService.getNearbyParkingSpots(40.7128, null, 5.0)
        );

        assertEquals("Invalid location parameters: latitude, longitude and radiusKm must be provided", exception.getMessage());
    }

    @Test
    void getNearbyParkingSpots_InvalidRadius() {
        // Act & Assert
        InvalidLocationParametersException exception = assertThrows(
                InvalidLocationParametersException.class,
                () -> parkingService.getNearbyParkingSpots(40.7128, -74.0060, -1.0)
        );

        assertEquals("Invalid location parameters: latitude, longitude and radiusKm must be provided", exception.getMessage());
    }

    @Test
    void getNearbyParkingSpots_ZeroRadius() {
        // Act & Assert
        InvalidLocationParametersException exception = assertThrows(
                InvalidLocationParametersException.class,
                () -> parkingService.getNearbyParkingSpots(40.7128, -74.0060, 0.0)
        );

        assertEquals("Invalid location parameters: latitude, longitude and radiusKm must be provided", exception.getMessage());
    }

    @Test
    void getNearbyParkingSpots_EmptyResult() {
        // Arrange
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double radiusKm = 5.0;

        when(parkingRepo.findNearbyParkingSpots(latitude, longitude, radiusKm))
                .thenReturn(Collections.emptyList());

        // Act
        List<ParkingSpotDetailResponse> response =
                parkingService.getNearbyParkingSpots(latitude, longitude, radiusKm);

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    private Parking createMockParkingListing() {
        Users users = new Users();
        users.setName("Abc");
        users.setPhone("324324234");

        Parking listing = new Parking();
        listing.setId(123L);
        listing.setOwner(users);
        listing.setDescription("Test Parking");
        listing.setEmergencyContact("123-456-7890");

        Address address = new Address();
        address.setAddressLine1("123 Test St");
        address.setCity("Test City");
        address.setLatitude(BigDecimal.valueOf(40.7128));
        address.setLongitude(BigDecimal.valueOf(-74.0060));
        listing.setAddress(address);

        listing.setIsOpen24Hours(false);
        listing.setStartTime(LocalTime.of(8, 0));
        listing.setEndTime(LocalTime.of(20, 0));
        listing.setAvailabilityDays(new HashSet<>(Arrays.asList(DayOfWeekEnum.WED)));

        Set<Amenity> facilities = new HashSet<>();
        facilities.add(createFacility("CCTV"));
        listing.setAmenities(facilities);

        Set<ParkingVehicleConfig> vehicleConfigs = new HashSet<>();
        ParkingVehicleConfig config = new ParkingVehicleConfig();
        config.setHourlyRate(BigDecimal.valueOf(10));
        config.setDailyRate(BigDecimal.valueOf(50));
        config.setWeeklyRate(BigDecimal.valueOf(300));
        config.setMonthlyRate(BigDecimal.valueOf(1000));
        vehicleConfigs.add(config);
        listing.setVehicleConfigs(vehicleConfigs);

        return listing;
    }

    private Amenity createFacility(String name) {
        Amenity amenity = new Amenity();
        amenity.setAmenityName(name);
        return amenity;
    }
}
