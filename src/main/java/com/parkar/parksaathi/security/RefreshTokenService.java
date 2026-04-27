package com.parkar.parksaathi.security;

import com.parkar.parksaathi.model.RefreshToken;
import com.parkar.parksaathi.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(UUID.randomUUID().toString())
                .timeToLive(refreshTokenExpiration / 1000) // Convert ms to seconds
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findById(token);
    }

    public RefreshToken verifyRefreshToken(RefreshToken token) {
        if (token.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }
        // Expiration is handled automatically by Redis via @TimeToLive
        // But if it was fetched, it hasn't expired in Redis yet
        return token;
    }

    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        // Revoke old token
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        // Create new token for the same user
        return createRefreshToken(oldToken.getUserId());
    }

    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findById(token)
        .orElseThrow(() -> new RuntimeException("Invalid or Expired Refresh token"));

    refreshToken.setRevoked(true);
    refreshTokenRepository.save(refreshToken);
    }
}
