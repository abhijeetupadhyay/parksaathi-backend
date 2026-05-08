package com.parkar.parksaathi.model;

import com.parkar.parksaathi.enums.DayOfWeekEnum;
import com.parkar.parksaathi.enums.ListingStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "parking_listings")
@Data
@EqualsAndHashCode(exclude = {"address", "owner", "availabilityDays", "facilities", "vehicleConfigs", "images"})
@ToString(exclude = {"address", "owner", "availabilityDays", "facilities", "vehicleConfigs", "images"})
public class ParkingListing {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id")
    private Long ownerId;
    
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "address_id")
    private Address address;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "emergency_contact", length = 20)
    private String emergencyContact;
    
    @Column(name = "is_open_24_hours")
    private Boolean isOpen24Hours = false;
    
    @Column(name = "start_time")
    private LocalTime startTime;
    
    @Column(name = "end_time")
    private LocalTime endTime;
    
    @Column(name = "ad_start_date", nullable = false)
    private LocalDate adStartDate;
    
    @Column(name = "ad_end_date", nullable = false)
    private LocalDate adEndDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ListingStatus status = ListingStatus.ACTIVE;
    
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
    
    @ElementCollection(targetClass = DayOfWeekEnum.class)
    @CollectionTable(
        name = "parking_availability_days",
        joinColumns = @JoinColumn(name = "parking_id")
    )
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeekEnum> availabilityDays;
    
    @ManyToMany
    @JoinTable(
        name = "parking_facilities_mapping",
        joinColumns = @JoinColumn(name = "parking_id"),
        inverseJoinColumns = @JoinColumn(name = "facility_id")
    )
    private Set<Facility> facilities;
    
    @OneToMany(mappedBy = "parking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ParkingVehicleConfig> vehicleConfigs;
    
}
