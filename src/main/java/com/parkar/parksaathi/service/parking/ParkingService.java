package com.parkar.parksaathi.service.parking;

import com.parkar.parksaathi.dto.request.AddParkingRequest;
import com.parkar.parksaathi.dto.request.CreateParkingResponse;
import com.parkar.parksaathi.dto.response.*;
import com.parkar.parksaathi.exception.customexceptions.InvalidLocationParametersException;
import com.parkar.parksaathi.exception.customexceptions.ResourceNotFoundException;
import com.parkar.parksaathi.model.*;
import com.parkar.parksaathi.repository.AmenityRepository;
import com.parkar.parksaathi.repository.ParkingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ParkingService {

    private final ParkingRepository parkingRepository;

    private final AmenityRepository amenityRepository;

    public CreateParkingResponse addNewParking(AddParkingRequest req, Long currentUserId) {
        Parking listing = new Parking();
        Users owner = new Users();
        owner.setId(currentUserId);
        listing.setOwner(owner);
        listing.setDescription(req.getDescription());
        listing.setEmergencyContact(req.getEmergencyContact());

        // Map Address
        Address addr = new Address();
        addr.setAddressLine1(req.getAddress().getAddressLine1());
        addr.setAddressLine2(req.getAddress().getAddressLine2());
        addr.setCity(req.getAddress().getCity());
        addr.setLatitude(req.getAddress().getLatitude());
        addr.setLongitude(req.getAddress().getLongitude());
        listing.setAddress(addr);

        // Map Availability
        listing.setIsOpen24Hours(req.getAvailability().isOpen24Hours());
        listing.setStartTime(req.getAvailability().getStartTime());
        listing.setEndTime(req.getAvailability().getEndTime());
        listing.setAdStartDate(req.getAvailability().getAdStartDate());
        listing.setAdEndDate(req.getAvailability().getAdEndDate());
        listing.setAvailabilityDays(new HashSet<>(req.getAvailability().getDays()));

        // Map Facilities (Lookup from DB by Name)
        listing.setAmenities(amenityRepository.findByAmenityNameIn(req.getAmenities()));

        // Map Vehicle Configs
        listing.setVehicleConfigs(req.getVehicleConfigs().stream().map(vDto -> {
            ParkingVehicleConfig config = new ParkingVehicleConfig();

            // Create VehicleType entity reference
            VehicleType vehicleType = new VehicleType();
            vehicleType.setId(vDto.getVehicleTypeId());
            config.setVehicleType(vehicleType);

            config.setMaxCapacity(vDto.getMaxCapacity());
            config.setHourlyRate(vDto.getHourlyRate());
            config.setDailyRate(vDto.getDailyRate());
            config.setWeeklyRate(vDto.getWeeklyRate());
            config.setMonthlyRate(vDto.getMonthlyRate());
            config.setParking(listing);
            return config;
        }).collect(Collectors.toSet()));

        Long parkingId = parkingRepository.save(listing).getId();
        CreateParkingResponse response = CreateParkingResponse.builder()
                .parkingId(parkingId)
                .message("Parking spot created successfully")
                .createdAt(LocalDateTime.now())
                .build();
        return response;
    }

    public ParkingSpotDetailResponse getParkingDetail(Long spotId) {
        Parking parkingSpot = parkingRepository.findById(spotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking spot not found with id: " + spotId));

        // Get pricing from parking_vehicle_configs
        Set<ParkingVehicleConfig> vehicleConfigs = parkingSpot.getVehicleConfigs();

        // Calculate average pricing across all vehicle types
        double avgHourly =  vehicleConfigs.stream()
                .map(ParkingVehicleConfig::getHourlyRate)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average().orElse(0.0);
        double avgDaily = vehicleConfigs.stream()
                .map(ParkingVehicleConfig::getDailyRate)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average().orElse(0.0);
        double avgWeekly =  vehicleConfigs.stream()
                .map(ParkingVehicleConfig::getWeeklyRate)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average().orElse(0.0);
        double avgMonthly =  vehicleConfigs.stream()
                .map(ParkingVehicleConfig::getMonthlyRate)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average().orElse(0.0);

        /* Fetch rating from parking_rating_aggregate or calculate from parking_ratings
        ParkingRatingAggregate ratingAggregate = parkingRatingAggregateRepository.findByParkingId(spotId)
                .orElse(ParkingRatingAggregate.builder()
                        .averageRating(0.0)
                        .totalRatings(0)
                        .build());*/

        return ParkingSpotDetailResponse.builder()
                .parkingInfo(ParkingInfo.builder()
                        .parkingId(String.valueOf(parkingSpot.getId()))
                        .parkingName(parkingSpot.getDescription()) // Using description as name
                        .parkingAddress(parkingSpot.getAddress().getAddressLine1() + ", " +
                                parkingSpot.getAddress().getCity())
                        .aboutParking(parkingSpot.getDescription())
                        .amenities(parkingSpot.getAmenities().stream()
                                .map(Amenity::getAmenityName)
                                .collect(Collectors.toList()))
                        .build())
                .pricingInfo(PricingInfo.builder()
                        .hourly(avgHourly)
                        .daily(avgDaily)
                        .weekly(avgWeekly)
                        .monthly(avgMonthly)
                        .build())
                /* .rating(RatingInfo.builder()
                         .rating(ratingAggregate.getAverageRating())
                         .ratingCount(ratingAggregate.getTotalRatings())
                         .build())*/
                .availabilityInfo(AvailabilityInfo.builder()
                        .weekly(!parkingSpot.getAvailabilityDays().isEmpty())
                        .open24x7(parkingSpot.getIsOpen24Hours())
                        .build())
                .build();
    }


    public List<ParkingSpotDetailResponse> getNearbyParkingSpots(Double latitude, Double longitude, Double radiusKm) {
        if (latitude == null || longitude == null || radiusKm == null || radiusKm <= 0) {
            throw new InvalidLocationParametersException("Invalid location parameters: latitude, longitude and radiusKm must be provided");
        }

        // Find all parking listings within the radius
        List<Parking> nearbyListings = parkingRepository.findNearbyParkingSpots(
                latitude, longitude, radiusKm);

        // Convert to response DTOs
        return nearbyListings.stream()
                .map(this::convertToDetailResponse)
                .collect(Collectors.toList());
    }

    private ParkingSpotDetailResponse convertToDetailResponse(Parking parkingSpot) {
        // Get pricing from parking_vehicle_configs
        Set<ParkingVehicleConfig> vehicleConfigs = parkingSpot.getVehicleConfigs();

        // Calculate average pricing across all vehicle types
        double avgHourly = vehicleConfigs.stream()
                .map(ParkingVehicleConfig::getHourlyRate)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average().orElse(0.0);
        double avgDaily = vehicleConfigs.stream()
                .map(ParkingVehicleConfig::getDailyRate)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average().orElse(0.0);
        double avgWeekly = vehicleConfigs.stream()
                .map(ParkingVehicleConfig::getWeeklyRate)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average().orElse(0.0);
        double avgMonthly = vehicleConfigs.stream()
                .map(ParkingVehicleConfig::getMonthlyRate)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average().orElse(0.0);

        // Build response
        return ParkingSpotDetailResponse.builder()
                .parkingInfo(ParkingInfo.builder()
                        .parkingId(String.valueOf(parkingSpot.getId()))
                        .parkingName(parkingSpot.getDescription())
                        .parkingAddress(parkingSpot.getAddress().getAddressLine1() + ", " +
                                parkingSpot.getAddress().getCity())
                        .aboutParking(parkingSpot.getDescription())
                        .amenities(parkingSpot.getAmenities().stream()
                                .map(Amenity::getAmenityName)
                                .collect(Collectors.toList()))
                        .build())
                .pricingInfo(PricingInfo.builder()
                        .hourly(avgHourly)
                        .daily(avgDaily)
                        .weekly(avgWeekly)
                        .monthly(avgMonthly)
                        .build())
                .ratingInfo(RatingInfo.builder()
                        .rating(4.5) // Default rating for now
                        .ratingCount(0)
                        .build())
                .availabilityInfo(AvailabilityInfo.builder()
                        .weekly(!parkingSpot.getAvailabilityDays().isEmpty())
                        .open24x7(parkingSpot.getIsOpen24Hours())
                        .build())
                .build();
    }

}