package com.parkar.parksaathi.repository;

import com.parkar.parksaathi.enums.ListingStatus;
import com.parkar.parksaathi.model.ParkingListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ParkingListingRepository extends JpaRepository<ParkingListing, Long> {
    
    @Query(value = "SELECT p.* FROM parking_listings p " +
            "JOIN address a ON p.address_id = a.id " +
            "WHERE (6371 * acos(cos(radians(:latitude)) * cos(radians(a.latitude)) * " +
            "cos(radians(a.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(a.latitude)))) <= :radiusKm " +
            "ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(a.latitude)) * " +
            "cos(radians(a.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(a.latitude))))",
            nativeQuery = true)
    List<ParkingListing> findNearbyParkingSpots(@Param("latitude") Double latitude,
                                                @Param("longitude") Double longitude,
                                                @Param("radiusKm") Double radiusKm);


}
