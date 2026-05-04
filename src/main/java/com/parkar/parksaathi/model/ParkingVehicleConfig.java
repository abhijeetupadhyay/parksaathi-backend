package com.parkar.parksaathi.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "parking_vehicle_configs", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"parking_id", "vehicle_type_id"}))
@Data
@EqualsAndHashCode(exclude = {"parking", "vehicleType"})
@ToString(exclude = {"parking", "vehicleType"})
public class ParkingVehicleConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_id", nullable = false)
    private ParkingListing parking;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_type_id", nullable = false)
    private VehicleType vehicleType;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity = 0;
    
    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate = BigDecimal.ZERO;
    
    @Column(name = "daily_rate", precision = 10, scale = 2)
    private BigDecimal dailyRate = BigDecimal.ZERO;
    
    @Column(name = "weekly_rate", precision = 10, scale = 2)
    private BigDecimal weeklyRate = BigDecimal.ZERO;
    
    @Column(name = "monthly_rate", precision = 10, scale = 2)
    private BigDecimal monthlyRate = BigDecimal.ZERO;
}
