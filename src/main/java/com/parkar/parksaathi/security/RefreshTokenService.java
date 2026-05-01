package com.parkar.parksaathi.security;

import com.parkar.parksaathi.exception.customexceptions.ResourceNotFoundException;
import com.parkar.parksaathi.exception.customexceptions.UnauthorizedException;
import com.parkar.parksaathi.model.RefreshToken;
import com.parkar.parksaathi.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public RefreshToken createRefreshToken(Long userId) {
        log.atInfo().log("SERVICE: createRefreshToken");
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(UUID.randomUUID().toString())
                .timeToLive(refreshTokenExpiration / 1000) // Convert ms to seconds
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        log.atInfo().log("SERVICE: findByToken");
        return refreshTokenRepository.findById(token);
    }

    public RefreshToken verifyRefreshToken(RefreshToken token) {
        log.atInfo().log("SERVICE: verifyRefreshToken");
        if (token.isRevoked()) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }
        // Expiration is handled automatically by Redis via @TimeToLive
        // But if it was fetched, it hasn't expired in Redis yet
        return token;
    }

    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        log.atInfo().log("SERVICE: rotateRefreshToken");
        // Revoke old token
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        // Create new token for the same user
        return createRefreshToken(oldToken.getUserId());
    }

    public void revokeRefreshToken(String token) {
        log.atInfo().log("SERVICE: revokeRefreshToken");
        RefreshToken refreshToken = refreshTokenRepository.findById(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or Expired Refresh token"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }
}
