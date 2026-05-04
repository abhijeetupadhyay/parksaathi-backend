package com.parkar.parksaathi.service.parking;

import com.parkar.parksaathi.dto.request.AddParkingRequest;
import com.parkar.parksaathi.model.Address;
import com.parkar.parksaathi.model.ParkingListing;
import com.parkar.parksaathi.repository.FacilityRepository;
import com.parkar.parksaathi.repository.ParkingListingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ParkingService {

    private final ParkingListingRepository parkingRepo;

    private final FacilityRepository facilityRepo;
    private final AddressRepository addressRepo;

    public Long addNewParking(AddParkingRequest req, Long currentUserId) {
        ParkingListing listing = new ParkingListing();
        listing.setOwnerId(currentUserId);
        listing.setDescription(req.getDescription());
        listing.setEmergencyContact(req.getEmergencyContact());
        
        // Map Address
        Address addr = new Address();
        addr.setAddressLine1(req.getAddress().getAddressLine1());
        addr.setCity(req.getAddress().getCity());
        addr.setPincode(req.getAddress().getPincode()); // Pincode is in your design
        addr.setLatitude(req.getAddress().getLatitude());
        addr.setLongitude(req.getAddress().getLongitude());
        listing.setAddress(addr);

        // Map Availability
        listing.setOpen24Hours(req.getAvailability().isOpen24Hours());
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
            config.setVehicleTypeId(vDto.getVehicleTypeId());
            config.setMaxCapacity(vDto.getMaxCapacity());
            config.setHourlyRate(vDto.getHourlyRate());
            config.setDailyRate(vDto.getDailyRate());
            config.setWeeklyRate(vDto.getWeeklyRate());
            config.setMonthlyRate(vDto.getMonthlyRate());
            config.setParking(listing);
            return config;
        }).collect(Collectors.toList()));

        // Map Images
        listing.setImages(req.getImageUrls().stream().map(url -> {
            ParkingImage img = new ParkingImage();
            img.setImageUrl(url);
            img.setParking(listing);
            return img;
        }).collect(Collectors.toList()));

        return parkingRepo.save(listing).getId();
    }
}