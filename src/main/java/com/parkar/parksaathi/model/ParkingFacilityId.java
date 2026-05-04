package com.parkar.parksaathi.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class ParkingFacilityId implements Serializable {
    private Long parkingId;
    private Integer facilityId;
}
