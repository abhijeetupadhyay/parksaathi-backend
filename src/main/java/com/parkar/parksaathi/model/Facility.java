package com.parkar.parksaathi.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "facilities")
@Data
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "facility_name", unique = true, nullable = false)
    private String facilityName;
}
