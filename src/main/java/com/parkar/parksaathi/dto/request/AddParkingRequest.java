package com.parkar.parksaathi.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class AddParkingRequest {

    private String name;
    private String description;
    private String status;
    private String emergencyContact;
    private Boolean isApprovalRequired;
    private AddressDto address;
    private AvailabilityDto availability;
    private List<String> amenities;
    private List<VehicleConfigDto> vehicleConfigs;
    private List<String> imageUrls;

}
