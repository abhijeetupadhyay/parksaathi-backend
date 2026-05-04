package com.parkar.parksaathi.repository;

import com.parkar.parksaathi.model.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Integer> {
    
    Optional<Facility> findByFacilityName(String facilityName);
    
    boolean existsByFacilityName(String facilityName);

    Set<Facility> findByFacilityNameIn(List<String> names);
}
