package com.parkar.parksaathi.service.parking;

import com.parkar.parksaathi.dto.request.AddParkingRequest;
import com.parkar.parksaathi.dto.request.AddressDto;
import com.parkar.parksaathi.dto.request.CreateParkingResponse;
import com.parkar.parksaathi.dto.request.VehicleConfigDto;
import com.parkar.parksaathi.dto.response.*;
import com.parkar.parksaathi.enums.ListingStatus;
import com.parkar.parksaathi.exception.customexceptions.InvalidLocationParametersException;
import com.parkar.parksaathi.exception.customexceptions.ResourceNotFoundException;
import com.parkar.parksaathi.model.*;
import com.parkar.parksaathi.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ParkingService {

    private final ParkingRepository parkingRepository;
    private final AmenityRepository amenityRepository;
    private final AddressRepository addressRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final ParkingVehicleConfigRepository parkingVehicleConfigRepository;

    /**
     * Method to add new parking
     *
     * @param req
     * @param currentUser
     * @return
     */
    public CreateParkingResponse addNewParking(AddParkingRequest req, Users currentUser) {
        Parking listing = new Parking();
        listing.setOwner(currentUser);
        listing.setName(req.getName());
        listing.setDescription(req.getDescription());
        listing.setStatus(ListingStatus.valueOf(req.getStatus()));
        listing.setEmergencyContact(req.getEmergencyContact());
        listing.setIsApprovalRequired(req.getIsApprovalRequired());
        // Map Availability
        listing.setIsOpen24Hours(req.getAvailability().getIsOpen24Hours());
        listing.setStartTime(req.getAvailability().getStartTime());
        listing.setEndTime(req.getAvailability().getEndTime());
        listing.setAdStartDate(req.getAvailability().getAdStartDate());
        listing.setAdEndDate(req.getAvailability().getAdEndDate());
        listing.setAvailabilityDays(new HashSet<>(req.getAvailability().getDays()));
        listing.setAmenities(getAmenities(req));
        // Map Address
        listing.setAddress(getAddress(req));
        Parking parking;
        try {
            parking = parkingRepository.save(listing);
            log.atInfo().log("Parking details are added with id" + parking.getId());
        } catch (Exception exception) {
            log.atError().log("Adding parking details failed with exception: " + exception);
            throw new RuntimeException("Adding new parking failed with exception: " + exception);
        }
        addVehicleConfigsToParking(req.getVehicleConfigs(), parking);
        log.atInfo().log("Vehicle configs are added to the parking");
        CreateParkingResponse response = CreateParkingResponse.builder()
                .parkingId(parking.getId())
                .createdAt(LocalDateTime.now())
                .build();
        return response;
    }

    /**
     * This method will add vehicle configs to Parking
     *
     * @param vehicleConfigDtos
     * @param parking
     */
    private void addVehicleConfigsToParking(List<VehicleConfigDto> vehicleConfigDtos, Parking parking) {
        for(VehicleConfigDto vehicleConfigDto : vehicleConfigDtos) {
            ParkingVehicleConfig parkingVehicleConfig = new ParkingVehicleConfig();
            Optional<VehicleType> vehicleTypeOptional =
                    vehicleTypeRepository.findById(vehicleConfigDto.getVehicleTypeId());
            if (vehicleTypeOptional.isPresent()) {
                parkingVehicleConfig.setVehicleType(vehicleTypeOptional.get());
                parkingVehicleConfig.setParking(parking);
                parkingVehicleConfig.setMaxCapacity(vehicleConfigDto.getMaxCapacity());
                parkingVehicleConfig.setHourlyRate(vehicleConfigDto.getHourlyRate());
                parkingVehicleConfig.setDailyRate(vehicleConfigDto.getDailyRate());
                parkingVehicleConfig.setWeeklyRate(vehicleConfigDto.getWeeklyRate());
                parkingVehicleConfig.setMonthlyRate(vehicleConfigDto.getMonthlyRate());
                ParkingVehicleConfig vehicleConfig;
                try {
                    vehicleConfig = parkingVehicleConfigRepository.save(parkingVehicleConfig);
                    log.atInfo().log("Vehicle Configs details are added with id" + vehicleConfig.getId());
                } catch (Exception e) {
                    log.atError().log("Adding vehicle configs for parking failed with exception: " + e);
                    throw new RuntimeException("Adding vehicle configs for parking failed with exception: " + e);
                }
            } else {
                throw new ResourceNotFoundException("Invalid Vehicle Type!");
            }
        }
    }

    /**
     * This method will get the address from DB on the basis of latitude and longitude
     *
     * @param req
     * @return
     */
    private Address getAddress(AddParkingRequest req) {
        Optional<Address> addressOptional =
                addressRepository.findByLatitudeAndLongitude(
                        req.getAddress().getLatitude(), req.getAddress().getLongitude());
        Address address = addressOptional.isPresent() ? addressOptional.get() : saveAddress(req.getAddress());
        return address;
    }

    /**
     * This method will save the address to DB
     *
     * @param addressDto
     * @return
     */
    private Address saveAddress(AddressDto addressDto) {
        Address address = new Address();
        address.setAddressLine1(addressDto.getAddressLine1());
        address.setAddressLine2(addressDto.getAddressLine2());
        address.setCity(addressDto.getCity());
        address.setState(addressDto.getState());
        address.setLatitude(addressDto.getLatitude());
        address.setLongitude(addressDto.getLongitude());
        return addressRepository.save(address);
    }

    /**
     * This method will return the amenities if they exist in DB, else those will be created
     *
     * @param req
     * @return
     */
    private Set<Amenity> getAmenities(AddParkingRequest req) {
        // Set Amenities
        Set<Amenity> existingAmenities = amenityRepository.findByAmenityNameIn(req.getAmenities());
        Set<String> existingAmenitiesNames = existingAmenities.stream().
                map(Amenity::getAmenityName).collect(Collectors.toSet());
        List<Amenity> newAmenities = req.getAmenities().stream()
                .filter(name -> !existingAmenitiesNames.contains(name))
                .map(name -> {
                    Amenity amenity = new Amenity();
                    amenity.setAmenityName(name);
                    return amenity;
                })
                .toList();
        if(!newAmenities.isEmpty()) {
            amenityRepository.saveAll(newAmenities);
        }
        Set<Amenity> allAmenities = new HashSet<>(existingAmenities);
        existingAmenities.addAll(newAmenities);
        return allAmenities;
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