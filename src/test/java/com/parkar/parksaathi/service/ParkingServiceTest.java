package com.parkar.parksaathi.service;

import com.parkar.parksaathi.dto.request.*;
import com.parkar.parksaathi.dto.response.ParkingDetailsResponse;
import com.parkar.parksaathi.enums.DayOfWeekEnum;
import com.parkar.parksaathi.exception.customexceptions.InvalidLocationParametersException;
import com.parkar.parksaathi.exception.customexceptions.ResourceNotFoundException;
import com.parkar.parksaathi.model.*;
import com.parkar.parksaathi.repository.*;
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
    private AmenityRepository amenityRepo;

    @Mock
    private AddressRepository addressRepo;

    @Mock
    private VehicleTypeRepository vehicleTypeRepo;

    @Mock
    private ParkingVehicleConfigRepository parkingVehicleConfigRepo;

    @InjectMocks
    private ParkingService parkingService;

    private AddParkingRequest validRequest;
    private Parking mockParking;
    private Users mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new Users();
        mockUser.setId(1L);
        mockUser.setName("Test User");

        // Setup valid request
        validRequest = new AddParkingRequest();
        validRequest.setName("Test Parking");
        validRequest.setDescription("Test Description");
        validRequest.setStatus("ACTIVE");
        validRequest.setEmergencyContact("123-456-7890");
        validRequest.setIsApprovalRequired(false);

        AddressDto addressDto = new AddressDto();
        addressDto.setAddressLine1("123 Test St");
        addressDto.setCity("Test City");
        addressDto.setLatitude(BigDecimal.valueOf(40.7128));
        addressDto.setLongitude(BigDecimal.valueOf(-74.0060));
        validRequest.setAddress(addressDto);

        AvailabilityDto availabilityDto = new AvailabilityDto();
        availabilityDto.setIsOpen24Hours(false);
        availabilityDto.setStartTime(LocalTime.of(8, 0));
        availabilityDto.setEndTime(LocalTime.of(20, 0));
        availabilityDto.setAdStartDate(LocalDate.now());
        availabilityDto.setAdEndDate(LocalDate.now().plusMonths(1));
        availabilityDto.setDays(new HashSet<>(Arrays.asList(DayOfWeekEnum.WED)));
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
        Long expectedSpotId = 123L;

        Set<Amenity> mockFacilities = new HashSet<>();
        mockFacilities.add(createFacility("CCTV"));
        mockFacilities.add(createFacility("Security"));

        when(amenityRepo.findByAmenityNameIn(anyList())).thenReturn(mockFacilities);
        when(parkingRepo.save(any(Parking.class))).thenAnswer(invocation -> {
            Parking saved = invocation.getArgument(0);
            saved.setId(expectedSpotId);
            return saved;
        });

        VehicleType vehicleType = new VehicleType();
        vehicleType.setId(1);
        vehicleType.setTypeName("CAR");
        when(vehicleTypeRepo.findById(1)).thenReturn(Optional.of(vehicleType));
        when(parkingVehicleConfigRepo.save(any(ParkingVehicleConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CreateParkingResponse response = parkingService.addNewParking(validRequest, mockUser);

        // Assert
        assertNotNull(response);
        assertEquals(expectedSpotId, response.getParkingId());
        assertNotNull(response.getCreatedAt());

        // Verify repository interactions
        verify(parkingRepo, times(1)).save(any(Parking.class));

        // Verify saved entity
        ArgumentCaptor<Parking> captor = ArgumentCaptor.forClass(Parking.class);
        verify(parkingRepo).save(captor.capture());
        Parking savedListing = captor.getValue();

        assertEquals(mockUser.getId(), savedListing.getOwner().getId());
        assertEquals(validRequest.getName(), savedListing.getName());
        assertEquals(validRequest.getEmergencyContact(), savedListing.getEmergencyContact());
        assertFalse(savedListing.getIsOpen24Hours());
    }

    @Test
    void addNewParking_WithNewAmenities() {
        // Arrange
        validRequest.setAmenities(Arrays.asList("New Amenity"));
        when(amenityRepo.findByAmenityNameIn(anyList())).thenReturn(new HashSet<>());
        when(parkingRepo.save(any(Parking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        VehicleType vehicleType = new VehicleType();
        vehicleType.setId(1);
        vehicleType.setTypeName("CAR");
        when(vehicleTypeRepo.findById(1)).thenReturn(Optional.of(vehicleType));
        when(parkingVehicleConfigRepo.save(any(ParkingVehicleConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        parkingService.addNewParking(validRequest, mockUser);

        // Assert
        verify(amenityRepo, times(1)).saveAll(anyList());
    }

    @Test
    void addNewParking_InvalidVehicleType() {
        // Arrange
        when(amenityRepo.findByAmenityNameIn(anyList())).thenReturn(new HashSet<>());
        when(parkingRepo.save(any(Parking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vehicleTypeRepo.findById(anyInt())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> parkingService.addNewParking(validRequest, mockUser));
    }

    @Test
    void getParkingDetail_Success() {
        // Arrange
        Long spotId = 123L;
        when(parkingRepo.findById(spotId)).thenReturn(Optional.of(mockParking));

        // Act
        ParkingDetailsResponse response = parkingService.getParkingDetail(spotId);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getParkingInfo());
        assertEquals(123L, response.getParkingInfo().getId());
        assertEquals("Test Parking", response.getParkingInfo().getName());
        assertNotNull(response.getPricingAndCapacityInfo());
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
        List<ParkingDetailsResponse> response =
                parkingService.getNearbyParkingSpots(latitude, longitude, radiusKm);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());

        verify(parkingRepo, times(1)).findNearbyParkingSpots(latitude, longitude, radiusKm);
    }

    @Test
    void getNearbyParkingSpots_InvalidParameters() {
        // Act & Assert
        assertThrows(InvalidLocationParametersException.class, 
                () -> parkingService.getNearbyParkingSpots(null, -74.0060, 5.0));
        assertThrows(InvalidLocationParametersException.class, 
                () -> parkingService.getNearbyParkingSpots(40.7128, null, 5.0));
        assertThrows(InvalidLocationParametersException.class, 
                () -> parkingService.getNearbyParkingSpots(40.7128, -74.0060, -1.0));
    }

    private Parking createMockParkingListing() {
        Parking listing = new Parking();
        listing.setId(123L);
        listing.setOwner(mockUser);
        listing.setName("Test Parking");
        listing.setDescription("Test Description");
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
        VehicleType type = new VehicleType();
        type.setTypeName("CAR");
        config.setVehicleType(type);
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
