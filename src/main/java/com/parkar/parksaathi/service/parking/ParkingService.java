package com.parkar.parksaathi.service.parking;

import com.parkar.parksaathi.dto.request.AddParkingRequest;
import com.parkar.parksaathi.dto.response.ParkingSpotDetailResponse;
import com.parkar.parksaathi.model.*;
import com.parkar.parksaathi.repository.FacilityRepository;
import com.parkar.parksaathi.repository.ParkingListingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ParkingService {

    private final ParkingListingRepository parkingRepo;

    private final FacilityRepository facilityRepo;

    public Long addNewParking(AddParkingRequest req, Long currentUserId) {
        ParkingListing listing = new ParkingListing();
        listing.setOwnerId(currentUserId);
        listing.setDescription(req.getDescription());
        listing.setEmergencyContact(req.getEmergencyContact());
        
        // Map Address
        Address addr = new Address();
        addr.setAddressLine1(req.getAddress().getAddressLine1());
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
        listing.setFacilities(facilityRepo.findByFacilityNameIn(req.getAmenities()));

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

        return parkingRepo.save(listing).getId();
    }

    public ParkingSpotDetailResponse getSpotDetail(Long spotId) {
        ParkingListing parkingSpot = parkingRepo.findById(spotId)
                .orElseThrow(() -> new IllegalArgumentException("Parking spot not found with id: " + spotId));

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
                .spot(ParkingSpotDetailResponse.SpotInfo.builder()
                        .spotId(String.valueOf(parkingSpot.getId()))
                        .spotName(parkingSpot.getDescription()) // Using description as name
                        .spotAddress(parkingSpot.getAddress().getAddressLine1() + ", " +
                                parkingSpot.getAddress().getCity())
                        .aboutSpot(parkingSpot.getDescription())
                        .amenities(parkingSpot.getFacilities().stream()
                                .map(Facility::getFacilityName)
                                .collect(Collectors.toList()))
                        .build())
                .pricing(ParkingSpotDetailResponse.PricingInfo.builder()
                        .hourly(avgHourly)
                        .daily(avgDaily)
                        .weekly(avgWeekly)
                        .monthly(avgMonthly)
                        .build())
                /*.rating(ParkingSpotDetailResponse.RatingInfo.builder()
                        .rating(ratingAggregate.getAverageRating())
                        .ratingCount(ratingAggregate.getTotalRatings())
                        .build())*/
                .availability(ParkingSpotDetailResponse.AvailabilityInfo.builder()
                        .weekly(!parkingSpot.getAvailabilityDays().isEmpty())
                        .open24x7(parkingSpot.getIsOpen24Hours())
                        .build())
                .build();
    }


    private List<String> parseAmenities(String amenities) {
        if (amenities == null || amenities.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(amenities.split(","));
    }

}