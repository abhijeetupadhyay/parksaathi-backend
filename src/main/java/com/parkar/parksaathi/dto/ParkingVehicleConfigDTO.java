package com.parkar.parksaathi.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ParkingVehicleConfigDTO {
    private Long id;
    private Long parkingId;
    private Integer vehicleTypeId;
    private Integer maxCapacity;
    private BigDecimal hourlyRate;
    private BigDecimal dailyRate;
    private BigDecimal weeklyRate;
    private BigDecimal monthlyRate;
}