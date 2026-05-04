package com.parkar.parksaathi.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "parking_vehicle_configs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"parking_id", "vehicle_type_id"}))
@Data
public class ParkingVehicleConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "parking_id", nullable = false)
    private ParkingListing parking;

    @Column(name = "vehicle_type_id", nullable = false)
    private Integer vehicleTypeId;

    private Integer maxCapacity = 0;

    private BigDecimal hourlyRate = BigDecimal.ZERO;
    private BigDecimal dailyRate = BigDecimal.ZERO;
    private BigDecimal weeklyRate = BigDecimal.ZERO;
    private BigDecimal monthlyRate = BigDecimal.ZERO;
}
