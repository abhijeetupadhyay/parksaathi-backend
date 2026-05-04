package com.parkar.parksaathi.repository;

import com.parkar.parksaathi.model.ParkingVehicleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingVehicleConfigRepository extends JpaRepository<ParkingVehicleConfig, Long> {}

