package com.parkar.parksaathi.dto.response;

import com.parkar.parksaathi.dto.request.AvailabilityDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingDetailsResponse {
    private ParkingInfo parkingInfo;
    private PricingAndCapacityInfo pricingAndCapacityInfo;
    private RatingInfo ratingInfo;
    private AvailabilityDto availabilityInfo;
}
