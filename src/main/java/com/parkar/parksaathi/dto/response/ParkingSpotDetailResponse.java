package com.parkar.parksaathi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingSpotDetailResponse {
    private ParkingInfo parkingInfo;
    private PricingInfo pricingInfo;
    private RatingInfo ratingInfo;
    private AvailabilityInfo availabilityInfo;
}
