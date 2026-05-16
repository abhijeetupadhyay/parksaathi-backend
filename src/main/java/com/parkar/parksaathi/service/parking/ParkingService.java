package com.parkar.parksaathi.service.parking;

import com.parkar.parksaathi.dto.request.*;
import com.parkar.parksaathi.dto.response.*;
import com.parkar.parksaathi.enums.ListingStatus;
import com.parkar.parksaathi.exception.customexceptions.DuplicateParkingException;
import com.parkar.parksaathi.exception.customexceptions.InvalidLocationParametersException;
import com.parkar.parksaathi.exception.customexceptions.ResourceNotFoundException;
import com.parkar.parksaathi.model.*;
import com.parkar.parksaathi.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        Address address = getAddress(req);
        List<Parking> parkingList = parkingRepository.findParkingByOwnerAndAddress(currentUser, address);
        if (!parkingList.isEmpty()) {
            throw new DuplicateParkingException("Parking already exists for current user and address");
        }
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
        listing.setAddress(address);
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

    /**
     * This method will return the parking details on the basis of parking id
     *
     * @param parkingId
     * @return
     */
    public ParkingDetailsResponse getParkingDetail(Long parkingId) {
        Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking spot not found with id: " + parkingId));
        return getParkingDetailsResponse(parking);
    }

    /**
     * This method will return parking details on the basis of parking
     *
     * @param parking
     * @return
     */
    private ParkingDetailsResponse getParkingDetailsResponse(Parking parking) {
        return ParkingDetailsResponse.builder()
                .parkingInfo(getParkingInfo(parking))
                .availabilityInfo(getAvailabilityInfo(parking))
                .pricingAndCapacityInfo(getPricingAndCapacityInfo(parking.getVehicleConfigs()))
                .ratingInfo(getRatings(parking))
                .owner(getParkingOwnerDetails(parking.getOwner()))
                .build();
    }

    /**
     * This method will return the owner details of parking owner
     *
     * @param owner
     * @return
     */
    private ParkingOwner getParkingOwnerDetails(Users owner) {
        return ParkingOwner.builder()
                .name(owner.getName())
                .phone(owner.getPhone())
                .email(owner.getEmail())
                .aadhar(owner.getAadhaar())
                .status(String.valueOf(owner.getStatus()))
                .build();
    }

    /**
     * This method will return the rating for the parking
     *
     * @param parking
     * @return
     */
    private RatingInfo getRatings(Parking parking) {
        //todo:  parking ratings will be returned with this method
        return RatingInfo.builder().build();
    }

    /**
     * This method will return the pricing and capacity info for the parking
     *
     * @param vehicleConfigs
     * @return
     */
    private PricingAndCapacityInfo getPricingAndCapacityInfo(Set<ParkingVehicleConfig> vehicleConfigs) {
        Set<VehicleConfigWithTypeNameDto> vehicleConfigsWithName = vehicleConfigs.stream().map(e -> {
            VehicleConfigWithTypeNameDto vehicleConfig = new VehicleConfigWithTypeNameDto();
            vehicleConfig.setVehicleType(e.getVehicleType().getTypeName());
            vehicleConfig.setMaxCapacity(e.getMaxCapacity());
            vehicleConfig.setHourlyRate(e.getHourlyRate());
            vehicleConfig.setDailyRate(e.getDailyRate());
            vehicleConfig.setWeeklyRate(e.getWeeklyRate());
            vehicleConfig.setMonthlyRate(e.getMonthlyRate());
            return vehicleConfig;
        }).collect(Collectors.toSet());
        return PricingAndCapacityInfo.builder()
                .vehicleConfigs(vehicleConfigsWithName).build();
    }

    /**
     * This method will return the availability info for the parking
     *
     * @param parking
     * @return
     */
    private AvailabilityDto getAvailabilityInfo(Parking parking) {
        AvailabilityDto availabilityDto = new AvailabilityDto();
        availabilityDto.setIsOpen24Hours(parking.getIsOpen24Hours());
        availabilityDto.setStartTime(parking.getStartTime());
        availabilityDto.setEndTime(parking.getEndTime());
        availabilityDto.setAdStartDate(parking.getAdStartDate());
        availabilityDto.setAdEndDate(parking.getAdEndDate());
        availabilityDto.setDays(parking.getAvailabilityDays());
        return availabilityDto;
    }

    /**
     * This method will return the parking information
     *
     * @param parking
     * @return
     */
    private ParkingInfo getParkingInfo(Parking parking) {
        String address = getStringAddress(parking.getAddress());
        return ParkingInfo.builder()
                .id(parking.getId())
                .name(parking.getName())
                .description(parking.getDescription())
                .status(String.valueOf(parking.getStatus()))
                .address(address)
                .emergencyContact(parking.getEmergencyContact())
                .isApprovalRequired(parking.getIsApprovalRequired())
                .amenities(parking.getAmenities().stream().map(
                        e -> e.getAmenityName()).collect(Collectors.toSet()))
                .build();
    }

    /**
     * This method will return the address for the parking
     *
     * @param add
     * @return
     */
    private String getStringAddress(Address add) {
        return add.getAddressLine1() + " " + add.getAddressLine2() + " " + add.getCity() + " " + add.getState();
    }


    /**
     * This method will return nearby parking on the basis of latitude, longitude and radius
     *
     * @param latitude
     * @param longitude
     * @param radiusKm
     * @return
     */
    public List<ParkingDetailsResponse> getNearbyParkingSpots(Double latitude, Double longitude, Double radiusKm) {
        if (latitude == null || longitude == null || radiusKm == null || radiusKm <= 0) {
            throw new InvalidLocationParametersException(
                    "Invalid location parameters: latitude, longitude and radiusKm must be provided");
        }

        // Find all parking listings within the radius
        List<Parking> nearbyListings = parkingRepository.findNearbyParkingSpots(
                latitude, longitude, radiusKm);

        // Convert to response DTOs
        return nearbyListings.stream()
                .map(this::getParkingDetailsResponse)
                .collect(Collectors.toList());
    }
}