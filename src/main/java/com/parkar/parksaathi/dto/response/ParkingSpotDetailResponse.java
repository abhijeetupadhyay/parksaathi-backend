package com.parkar.parksaathi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingSpotDetailResponse {
    private SpotInfo spot;
    private PricingInfo pricing;
    private RatingInfo rating;
    private AvailabilityInfo availability;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpotInfo {
        private String spotId;
        private String spotName;
        private String spotAddress;
        private String aboutSpot;
        private List<String> amenities;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingInfo {
        private Double hourly;
        private Double daily;
        private Double weekly;
        private Double monthly;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingInfo {
        private Double rating;
        private Integer ratingCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailabilityInfo {
        private Boolean weekly;
        private Boolean open24x7;
    }
}
