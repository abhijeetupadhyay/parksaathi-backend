package com.parkar.parksaathi.service;

import com.parkar.parksaathi.dto.request.SignupRequest;
import com.parkar.parksaathi.dto.response.AuthResponse;
import com.parkar.parksaathi.enums.UserStatus;
import com.parkar.parksaathi.exception.customexceptions.ResourceNotFoundException;
import com.parkar.parksaathi.model.RefreshToken;
import com.parkar.parksaathi.model.Users;
import com.parkar.parksaathi.repository.UserRepository;
import com.parkar.parksaathi.security.JwtService;
import com.parkar.parksaathi.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public UserStatus getUserStatus(String phone) {
        return userRepository.findByPhone(phone)
                .map(Users::getStatus)
                .orElse(UserStatus.PENDING);
    }

    public AuthResponse signIn(String phone) {
        Users user = userRepository.findByPhone(phone)
                .orElseGet(() -> {
                    log.info("Creating new user for phone: {}", phone);
                    Users newUser = new Users();
                    newUser.setPhone(phone);
                    newUser.setName("Batman");
                    newUser.setStatus(UserStatus.PENDING);
                    return userRepository.save(newUser);
                });

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        log.info("User {} signed in successfully", phone);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000) // convert to seconds
                .status(user.getStatus())
                .build();
    }

    public AuthResponse refreshToken(String refreshTokenStr) {
        RefreshToken storedToken = refreshTokenService.findByToken(refreshTokenStr)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        // Verify the token is valid (not expired, not revoked)
        refreshTokenService.verifyRefreshToken(storedToken);

        // Rotate: revoke old, create new
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(storedToken);

        // Generate new access token
        Users user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String newAccessToken = jwtService.generateAccessToken(user);

        log.info("Token refreshed for user ID: {}", user.getId());
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .status(user.getStatus())
                .build();
    }

    public void signOut(String refreshTokenStr) {
        refreshTokenService.revokeRefreshToken(refreshTokenStr);
        log.info("User signed out successfully");
    }

    public void signup(SignupRequest request, Users currentUser) {
        currentUser.setName(request.getName());
        currentUser.setEmail(request.getEmail());
        currentUser.setAadhaar(request.getAadhar());
        currentUser.setStatus(UserStatus.ACTIVE);
        userRepository.save(currentUser);
        log.info("User {} completed signup", currentUser.getPhone());
    }
}
