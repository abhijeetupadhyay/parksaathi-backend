package com.parkar.parksaathi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingInfo {
    private Long id;
    private String name;
    private String description;
    private String status;
    private String address;
    private String emergencyContact;
    private Boolean isApprovalRequired;
    private Set<String> amenities;
}