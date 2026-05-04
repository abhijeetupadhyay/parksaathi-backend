package com.parkar.parksaathi.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "facilities")
@Data
@EqualsAndHashCode(exclude = "parkingListings")
@ToString(exclude = "parkingListings")
public class Facility {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "facility_name", length = 50, unique = true, nullable = false)
    private String facilityName;
    
    @ManyToMany(mappedBy = "facilities")
    private Set<ParkingListing> parkingListings = new HashSet<>();
}
