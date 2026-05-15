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
public class ParkingInfo {
    private String parkingId;
    private String parkingName;
    private String parkingAddress;
    private String aboutParking;
    private List<String> amenities;
}