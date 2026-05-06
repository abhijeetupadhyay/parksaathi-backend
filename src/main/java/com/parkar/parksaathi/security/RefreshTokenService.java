package com.parkar.parksaathi.security;

import com.parkar.parksaathi.exception.customexceptions.TokenRefreshException;
import com.parkar.parksaathi.model.RefreshToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_PREFIX = "rfrsh_tkn:";
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public RefreshToken createRefreshToken(Long userId) {
        log.atInfo().log("SERVICE: createRefreshToken for userId: {}", userId);
        String token = UUID.randomUUID().toString();
        String key = REFRESH_TOKEN_PREFIX + token;

        redisTemplate.opsForValue().set(key, userId.toString(), refreshTokenExpiration, TimeUnit.MILLISECONDS);

        return RefreshToken.builder()
                .userId(userId)
                .token(token)
                .build();
    }

    public Optional<RefreshToken> findByToken(String token) {
        log.atInfo().log("SERVICE: findByToken");
        String key = REFRESH_TOKEN_PREFIX + token;
        String userIdStr = redisTemplate.opsForValue().get(key);

        if (userIdStr == null) {
            return Optional.empty();
        }

        return Optional.of(RefreshToken.builder()
                .token(token)
                .userId(Long.parseLong(userIdStr))
                .build());
    }

    public RefreshToken verifyRefreshToken(RefreshToken token) {
        log.atInfo().log("SERVICE: verifyRefreshToken");
        if (token == null) {
            throw new TokenRefreshException("null", "Refresh token is null");
        }
        // Ensure token is still present and valid in Redis.
        // Presence in Redis signifies validity as we use TTL for expiration.
        String key = REFRESH_TOKEN_PREFIX + token.getToken();
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            log.warn("Token validation failed: Token not found, or expired. {}", token.getToken());
            throw new TokenRefreshException(token.getToken(), "Refresh token was not found or has expired");
        }

        return token;
    }

    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        log.atInfo().log("SERVICE: rotateRefreshToken");
        // Delete old token
        revokeRefreshToken(oldToken.getToken());

        // Create new token for the same user
        return createRefreshToken(oldToken.getUserId());
    }

    public void revokeRefreshToken(String token) {
        log.atInfo().log("SERVICE: revokeRefreshToken");
        String key = REFRESH_TOKEN_PREFIX + token;
        Boolean deleted = redisTemplate.delete(key);
        if (Boolean.FALSE.equals(deleted)) {
            log.warn("Attempted to revoke non-existent or already expired token: {}", token);
        }
    }
}
