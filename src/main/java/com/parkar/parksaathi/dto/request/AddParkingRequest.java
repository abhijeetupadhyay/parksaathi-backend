package com.parkar.parksaathi.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class AddParkingRequest {

    private String description;
    private String emergencyContact;
    private AddressDto address;
    private AvailabilityDto availability;
    private List<String> amenities;
    private List<VehicleConfigDto> vehicleConfigs;
    private List<String> imageUrls;

}
