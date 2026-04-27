package com.parkar.parksaathi.repository;

import com.parkar.parksaathi.model.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
