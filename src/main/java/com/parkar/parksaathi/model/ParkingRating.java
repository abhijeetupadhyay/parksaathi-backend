package com.parkar.parksaathi.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parking_ratings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "spot_id")
    private Long spotId;
    
    @Column(name = "rating")
    private Double rating;
    
    @Column(name = "rating_count")
    private Integer ratingCount;
}
