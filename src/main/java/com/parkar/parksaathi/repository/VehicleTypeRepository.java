package com.parkar.parksaathi.repository;

import com.parkar.parksaathi.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, Integer> {
    // Add custom query methods as needed
}
