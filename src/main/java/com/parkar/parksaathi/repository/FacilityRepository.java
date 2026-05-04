package com.parkar.parksaathi.repository;

import com.parkar.parksaathi.model.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Integer> {
    List<Facility> findByFacilityNameIn(List<String> names);
    Optional<Facility> findByFacilityName(String name);
}
