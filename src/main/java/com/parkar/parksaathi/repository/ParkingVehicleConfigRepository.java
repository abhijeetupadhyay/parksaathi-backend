package com.parkar.parksaathi.repository;

import com.parkar.parksaathi.model.ParkingVehicleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingVehicleConfigRepository extends JpaRepository<ParkingVehicleConfig, Long> {
    
    List<ParkingVehicleConfig> findByParkingId(Long parkingId);
    
    List<ParkingVehicleConfig> findByVehicleTypeId(Integer vehicleTypeId);
    
    Optional<ParkingVehicleConfig> findByParkingIdAndVehicleTypeId(Long parkingId, Integer vehicleTypeId);
    
    @Query("SELECT pvc FROM ParkingVehicleConfig pvc WHERE pvc.parking.id = :parkingId AND pvc.maxCapacity > 0")
    List<ParkingVehicleConfig> findAvailableConfigsByParkingId(@Param("parkingId") Long parkingId);
}
