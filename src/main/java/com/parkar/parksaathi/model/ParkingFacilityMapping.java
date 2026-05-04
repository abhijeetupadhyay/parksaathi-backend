package com.parkar.parksaathi.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "parking_facilities_mapping")
@Data
public class ParkingFacilityMapping {

    @EmbeddedId
    private ParkingFacilityId id;

    @ManyToOne
    @MapsId("parkingId")
    @JoinColumn(name = "parking_id")
    private ParkingListing parking;

    @ManyToOne
    @MapsId("facilityId")
    @JoinColumn(name = "facility_id")
    private Facility facility;
}
