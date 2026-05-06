package com.parkar.parksaathi.security;

import com.parkar.parksaathi.exception.customexceptions.TokenRefreshException;
import com.parkar.parksaathi.model.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private long refreshTokenExpiration = 604800000L; // 7 days
    private static final String REFRESH_TOKEN_PREFIX = "rfrsh_tkn:";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiration", refreshTokenExpiration);
    }

    @Test
    void testCreateRefreshToken() {
        Long userId = 1L;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        RefreshToken result = refreshTokenService.createRefreshToken(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertNotNull(result.getToken());
        verify(valueOperations, times(1)).set(anyString(), eq(userId.toString()), eq(refreshTokenExpiration), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void testFindByToken_Success() {
        String token = "sample-token";
        Long userId = 1L;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(REFRESH_TOKEN_PREFIX + token)).thenReturn(userId.toString());

        Optional<RefreshToken> result = refreshTokenService.findByToken(token);

        assertTrue(result.isPresent());
        assertEquals(token, result.get().getToken());
        assertEquals(userId, result.get().getUserId());
    }

    @Test
    void testFindByToken_NotFound() {
        String token = "invalid-token";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(REFRESH_TOKEN_PREFIX + token)).thenReturn(null);

        Optional<RefreshToken> result = refreshTokenService.findByToken(token);

        assertTrue(result.isEmpty());
    }

    @Test
    void testVerifyRefreshToken_Success() {
        RefreshToken token = new RefreshToken();
        token.setToken("some-token");

        when(redisTemplate.hasKey(REFRESH_TOKEN_PREFIX + "some-token")).thenReturn(true);

        RefreshToken result = refreshTokenService.verifyRefreshToken(token);

        assertEquals(token, result);
    }

    @Test
    void testVerifyRefreshToken_NotFound() {
        RefreshToken token = new RefreshToken();
        token.setToken("invalid-token");

        when(redisTemplate.hasKey(REFRESH_TOKEN_PREFIX + "invalid-token")).thenReturn(false);

        assertThrows(TokenRefreshException.class, () -> refreshTokenService.verifyRefreshToken(token));
    }

    @Test
    void testVerifyRefreshToken_NullToken() {
        assertThrows(TokenRefreshException.class, () -> refreshTokenService.verifyRefreshToken(null));
    }

    @Test
    void testRotateRefreshToken() {
        RefreshToken oldToken = new RefreshToken();
        oldToken.setToken("old-token");
        oldToken.setUserId(1L);

        when(redisTemplate.delete(REFRESH_TOKEN_PREFIX + "old-token")).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        RefreshToken result = refreshTokenService.rotateRefreshToken(oldToken);

        assertNotNull(result);
        assertNotEquals("old-token", result.getToken());
        assertEquals(1L, result.getUserId());
        verify(redisTemplate, times(1)).delete(REFRESH_TOKEN_PREFIX + "old-token");
        verify(valueOperations, times(1)).set(anyString(), eq("1"), anyLong(), any());
    }

    @Test
    void testRevokeRefreshToken() {
        String token = "sample-token";
        when(redisTemplate.delete(REFRESH_TOKEN_PREFIX + token)).thenReturn(true);

        refreshTokenService.revokeRefreshToken(token);

        verify(redisTemplate, times(1)).delete(REFRESH_TOKEN_PREFIX + token);
    }
}
