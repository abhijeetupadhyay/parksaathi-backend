package com.parkar.parksaathi.security;

import com.parkar.parksaathi.exception.customexceptions.ResourceNotFoundException;
import com.parkar.parksaathi.exception.customexceptions.UnauthorizedException;
import com.parkar.parksaathi.model.RefreshToken;
import com.parkar.parksaathi.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private long refreshTokenExpiration = 604800000L; // 7 days

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiration", refreshTokenExpiration);
    }

    @Test
    void testCreateRefreshToken() {
        Long userId = 1L;
        RefreshToken mockToken = RefreshToken.builder()
                .userId(userId)
                .token("sample-token")
                .timeToLive(refreshTokenExpiration / 1000)
                .revoked(false)
                .build();

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mockToken);

        RefreshToken result = refreshTokenService.createRefreshToken(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("sample-token", result.getToken());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void testFindByToken() {
        String token = "sample-token";
        RefreshToken mockToken = new RefreshToken();
        mockToken.setToken(token);

        when(refreshTokenRepository.findById(token)).thenReturn(Optional.of(mockToken));

        Optional<RefreshToken> result = refreshTokenService.findByToken(token);

        assertTrue(result.isPresent());
        assertEquals(token, result.get().getToken());
    }

    @Test
    void testVerifyRefreshToken_Success() {
        RefreshToken token = new RefreshToken();
        token.setRevoked(false);

        RefreshToken result = refreshTokenService.verifyRefreshToken(token);

        assertEquals(token, result);
    }

    @Test
    void testVerifyRefreshToken_Revoked() {
        RefreshToken token = new RefreshToken();
        token.setRevoked(true);

        assertThrows(UnauthorizedException.class, () -> refreshTokenService.verifyRefreshToken(token));
    }

    @Test
    void testRotateRefreshToken() {
        RefreshToken oldToken = new RefreshToken();
        oldToken.setToken("old-token");
        oldToken.setUserId(1L);
        oldToken.setRevoked(false);

        RefreshToken newToken = new RefreshToken();
        newToken.setToken("new-token");
        newToken.setUserId(1L);

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(oldToken).thenReturn(newToken);

        RefreshToken result = refreshTokenService.rotateRefreshToken(oldToken);

        assertTrue(oldToken.isRevoked());
        assertNotNull(result);
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void testRevokeRefreshToken_Success() {
        String token = "sample-token";
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setRevoked(false);

        when(refreshTokenRepository.findById(token)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(refreshToken)).thenReturn(refreshToken);

        refreshTokenService.revokeRefreshToken(token);

        assertTrue(refreshToken.isRevoked());
        verify(refreshTokenRepository, times(1)).save(refreshToken);
    }

    @Test
    void testRevokeRefreshToken_NotFound() {
        String token = "invalid-token";
        when(refreshTokenRepository.findById(token)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> refreshTokenService.revokeRefreshToken(token));
    }
}
