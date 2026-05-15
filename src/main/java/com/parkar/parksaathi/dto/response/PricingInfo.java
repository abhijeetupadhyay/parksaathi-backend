package com.parkar.parksaathi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingInfo {
    private Double hourly;
    private Double daily;
    private Double weekly;
    private Double monthly;
}