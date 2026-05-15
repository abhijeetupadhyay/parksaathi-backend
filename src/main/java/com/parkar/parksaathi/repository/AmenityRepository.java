package com.parkar.parksaathi.repository;

import com.parkar.parksaathi.model.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Integer> {

    Optional<Amenity> findByAmenityName(String amenityName);

    boolean existsByAmenityName(String amenityName);

    Set<Amenity> findByAmenityNameIn(List<String> names);
}
