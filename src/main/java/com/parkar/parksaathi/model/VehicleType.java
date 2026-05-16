package com.parkar.parksaathi.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "vehicle_type")
@Data
public class VehicleType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "type_name", unique = true, nullable = false, length = 50)
    private String typeName;

}
