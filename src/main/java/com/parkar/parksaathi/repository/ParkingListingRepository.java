package com.parkar.parksaathi.repository;

import com.parkar.parksaathi.model.ParkingListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingListingRepository extends JpaRepository<ParkingListing, Long> {}
