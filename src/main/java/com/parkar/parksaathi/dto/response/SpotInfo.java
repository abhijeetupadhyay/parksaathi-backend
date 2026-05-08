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
public class SpotInfo {
    private String spotId;
    private String spotName;
    private String spotAddress;
    private String aboutSpot;
    private List<String> amenities;
}