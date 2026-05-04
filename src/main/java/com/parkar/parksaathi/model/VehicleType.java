package com.parkar.parksaathi.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vehicle_types")
@Data
@EqualsAndHashCode(exclude = "vehicleConfigs")
@ToString(exclude = "vehicleConfigs")
public class VehicleType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @OneToMany(mappedBy = "vehicleType")
    private Set<ParkingVehicleConfig> vehicleConfigs = new HashSet<>();
}
