package com.parkar.parksaathi.repository;

import com.parkar.parksaathi.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Address entity.
 * Provides CRUD operations and query method support.
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    // Add custom query methods here if needed
}