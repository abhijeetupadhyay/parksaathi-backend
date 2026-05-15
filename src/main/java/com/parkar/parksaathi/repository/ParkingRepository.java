package com.parkar.parksaathi.repository;

import com.parkar.parksaathi.model.Parking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingRepository extends JpaRepository<Parking, Long> {

    @Query(value = "SELECT p.* FROM parking_listings p " +
            "JOIN address a ON p.address_id = a.id " +
            "WHERE (6371 * acos(cos(radians(:latitude)) * cos(radians(a.latitude)) * " +
            "cos(radians(a.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(a.latitude)))) <= :radiusKm " +
            "ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(a.latitude)) * " +
            "cos(radians(a.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(a.latitude))))",
            nativeQuery = true)
    List<Parking> findNearbyParkingSpots(@Param("latitude") Double latitude,
                                         @Param("longitude") Double longitude,
                                         @Param("radiusKm") Double radiusKm);


}
