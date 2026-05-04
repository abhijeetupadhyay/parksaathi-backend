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
    
    List<ParkingListing> findByStatus(ListingStatus status);
    
    List<ParkingListing> findByOwnerId(Long ownerId);
    
    @Query("SELECT p FROM ParkingListing p WHERE p.adStartDate <= :date AND p.adEndDate >= :date")
    List<ParkingListing> findActiveListingsForDate(@Param("date") LocalDate date);
    
    @Query("SELECT p FROM ParkingListing p JOIN p.address a WHERE a.city = :city AND p.status = :status")
    List<ParkingListing> findByCityAndStatus(@Param("city") String city, @Param("status") ListingStatus status);
    
    List<ParkingListing> findByIsOpen24HoursTrue();
}
