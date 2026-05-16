package com.parkar.parksaathi.repository;

import com.parkar.parksaathi.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByLatitudeAndLongitude(BigDecimal latitude, BigDecimal longitude);
}
