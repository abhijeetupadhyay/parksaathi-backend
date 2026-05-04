package com.parkar.parksaathi.model;

import com.parkar.parksaathi.constant.DayOfWeekEnum;
import com.parkar.parksaathi.constant.ListingStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "parking_listings")
@Data
public class ParkingListing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id")
    private Long ownerId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

    private String description;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "is_open_24_hours")
    private boolean isOpen24Hours;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "ad_start_date")
    private LocalDate adStartDate;

    @Column(name = "ad_end_date")
    private LocalDate adEndDate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "listing_status")
    private ListingStatus status = ListingStatus.ACTIVE;

    @ElementCollection(targetClass = DayOfWeekEnum.class)
    @CollectionTable(name = "parking_availability_days", joinColumns = @JoinColumn(name = "parking_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeekEnum> availabilityDays;

    @ManyToMany
    @JoinTable(
        name = "parking_facilities_mapping",
        joinColumns = @JoinColumn(name = "parking_id"),
        inverseJoinColumns = @JoinColumn(name = "facility_id")
    )
    private List<Facility> facilities;

    @OneToMany(mappedBy = "parking", cascade = CascadeType.ALL)
    private List<ParkingVehicleConfig> vehicleConfigs;

    /*@OneToMany(mappedBy = "parking", cascade = CascadeType.ALL)
    private List<ParkingImage> images;*/
}