package com.parkar.parksaathi.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "parking_availability_days")
@Data
public class ParkingAvailabilityDay {

    @EmbeddedId
    private ParkingAvailabilityDayId id;

    @ManyToOne
    @MapsId("parkingId")
    @JoinColumn(name = "parking_id")
    private ParkingListing parking;
}
