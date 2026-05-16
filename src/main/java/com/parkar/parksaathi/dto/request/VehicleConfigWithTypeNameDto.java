package com.parkar.parksaathi.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VehicleConfigWithTypeNameDto {
    private String vehicleType;
    private Integer maxCapacity;
    private BigDecimal hourlyRate;
    private BigDecimal dailyRate;
    private BigDecimal weeklyRate;
    private BigDecimal monthlyRate;
}