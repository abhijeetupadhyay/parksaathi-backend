package com.parkar.parksaathi.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@Entity
@Table(name = "amenity")
@Data
@EqualsAndHashCode(exclude = "parkings")
@ToString(exclude = "parkings")
public class Amenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "amenity_name", length = 50, unique = true, nullable = false)
    private String amenityName;

    @ManyToMany(mappedBy = "amenities")
    private Set<Parking> parkings;
}
