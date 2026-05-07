package com.parkar.parksaathi.service;

import com.parkar.parksaathi.dto.request.SignupRequest;
import com.parkar.parksaathi.dto.response.AuthResponse;
import com.parkar.parksaathi.enums.UserStatus;
import com.parkar.parksaathi.model.RefreshToken;
import com.parkar.parksaathi.model.Users;
import com.parkar.parksaathi.repository.UserRepository;
import com.parkar.parksaathi.security.JwtService;
import com.parkar.parksaathi.security.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private Users testUser;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        testUser = new Users();
        testUser.setId(1L);
        testUser.setPhone("1234567890");
        testUser.setStatus(UserStatus.ACTIVE);

        testRefreshToken = new RefreshToken();
        testRefreshToken.setToken("sample-refresh-token");
        testRefreshToken.setUserId(1L);
    }

    @Test
    void testGetUserStatus_UserExists() {
        when(userRepository.findByPhone("1234567890")).thenReturn(Optional.of(testUser));

        UserStatus status = authService.getUserStatus("1234567890");
        assertEquals(UserStatus.ACTIVE, status);
    }

    @Test
    void testGetUserStatus_UserDoesNotExist() {
        when(userRepository.findByPhone("unknown")).thenReturn(Optional.empty());

        UserStatus status = authService.getUserStatus("unknown");
        assertEquals(UserStatus.PENDING, status);
    }

    @Test
    void testSignIn_ExistingUser() {
        when(userRepository.findByPhone("1234567890")).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(testUser)).thenReturn("sample-access-token");
        when(refreshTokenService.createRefreshToken(testUser.getId())).thenReturn(testRefreshToken);
        when(jwtService.getAccessTokenExpiration()).thenReturn(3600000L);

        AuthResponse response = authService.signIn("1234567890");

        assertNotNull(response);
        assertEquals("sample-access-token", response.getAccessToken());
        assertEquals("sample-refresh-token", response.getRefreshToken());
        assertEquals(3600, response.getExpiresIn());
        assertEquals(UserStatus.ACTIVE, response.getStatus());

        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    void testSignIn_NewUser() {
        when(userRepository.findByPhone("0987654321")).thenReturn(Optional.empty());
        Users newUser = new Users();
        newUser.setId(2L);
        newUser.setPhone("0987654321");
        newUser.setStatus(UserStatus.PENDING);

        when(userRepository.save(any(Users.class))).thenReturn(newUser);
        when(jwtService.generateAccessToken(newUser)).thenReturn("new-access-token");
        when(refreshTokenService.createRefreshToken(newUser.getId())).thenReturn(testRefreshToken);
        when(jwtService.getAccessTokenExpiration()).thenReturn(3600000L);

        AuthResponse response = authService.signIn("0987654321");

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals(UserStatus.PENDING, response.getStatus());
        verify(userRepository, times(1)).save(any(Users.class));
    }

    @Test
    void testRefreshToken_Success() {
        when(refreshTokenService.findByToken("sample-refresh-token")).thenReturn(Optional.of(testRefreshToken));

        RefreshToken newRT = new RefreshToken();
        newRT.setToken("new-refresh-token");
        newRT.setUserId(1L);
        when(refreshTokenService.rotateRefreshToken(testRefreshToken)).thenReturn(newRT);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(testUser)).thenReturn("new-access-token");
        when(jwtService.getAccessTokenExpiration()).thenReturn(3600000L);

        AuthResponse response = authService.refreshToken("sample-refresh-token");

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
    }

    @Test
    void testSignOut() {
        doReturn("USER_SIGNED_OUT").when(refreshTokenService).revokeRefreshToken("sample-refresh-token");
        authService.signOut("sample-refresh-token");
        verify(refreshTokenService, times(1)).revokeRefreshToken("sample-refresh-token");
    }

    @Test
    void testSignup() {
        SignupRequest request = new SignupRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setAadhar("123456789012");

        when(userRepository.save(testUser)).thenReturn(testUser);

        authService.signup(request, testUser);

        assertEquals("John Doe", testUser.getName());
        assertEquals("john@example.com", testUser.getEmail());
        assertEquals("123456789012", testUser.getAadhaar());
        assertEquals(UserStatus.ACTIVE, testUser.getStatus());
        verify(userRepository, times(1)).save(testUser);
    }
}
