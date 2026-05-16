package com.parkar.parksaathi.dto.response;

import com.parkar.parksaathi.dto.request.VehicleConfigWithTypeNameDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingAndCapacityInfo {
    private Set<VehicleConfigWithTypeNameDto> vehicleConfigs;
}