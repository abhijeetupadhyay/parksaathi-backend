package com.parkar.parksaathi.dto.request;

import com.parkar.parksaathi.constant.DayOfWeekEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
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

    @Data
    public static class AddressDto {
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String pincode;
        private BigDecimal latitude;
        private BigDecimal longitude;
    }

    @Data
    public static class AvailabilityDto {
        private boolean isOpen24Hours;
        private LocalTime startTime;
        private LocalTime endTime;
        private LocalDate adStartDate;
        private LocalDate adEndDate;
        private List<DayOfWeekEnum> days;
    }

    @Data
    public static class VehicleConfigDto {
        private Integer vehicleTypeId;
        private Integer maxCapacity;
        private BigDecimal hourlyRate;
        private BigDecimal dailyRate;
        private BigDecimal weeklyRate;
        private BigDecimal monthlyRate;
    }
}
