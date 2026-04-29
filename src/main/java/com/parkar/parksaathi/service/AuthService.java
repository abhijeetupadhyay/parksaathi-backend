package com.parkar.parksaathi.service;

import com.parkar.parksaathi.dto.AuthResponse;
import com.parkar.parksaathi.dto.SignupRequest;
import com.parkar.parksaathi.enums.UserStatus;
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
        log.atInfo().log("SERVICE: getUserStatus");
        return userRepository.findByPhone(phone)
                .map(Users::getStatus)
                .orElse(UserStatus.PENDING);
    }

    public AuthResponse signIn(String phone) {
        log.atInfo().log("SERVICE: signIn");
        Users user = userRepository.findByPhone(phone)
                .orElseGet(() -> {
                    Users newUser = new Users();
                    newUser.setPhone(phone);
                    newUser.setName("Batman");
                    newUser.setStatus(UserStatus.PENDING);
                    return userRepository.save(newUser);
                });

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000) // convert to seconds
                .status(user.getStatus())
                .build();
    }

    public AuthResponse refreshToken(String refreshTokenStr) {
        log.atInfo().log("SERVICE: refreshToken");
        RefreshToken storedToken = refreshTokenService.findByToken(refreshTokenStr)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        // Verify the token is valid (not expired, not revoked)
        refreshTokenService.verifyRefreshToken(storedToken);

        // Rotate: revoke old, create new
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(storedToken);

        // Generate new access token
        Users user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String newAccessToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .status(user.getStatus())
                .build();
    }

    public void signOut(String refreshTokenStr) {
        log.atInfo().log("SERVICE: signOut");
        refreshTokenService.revokeRefreshToken(refreshTokenStr);
    }

    public void signup(SignupRequest request, Users currentUser) {
        log.atInfo().log("SERVICE: signup");
        currentUser.setName(request.getName());
        currentUser.setEmail(request.getEmail());
        currentUser.setAadhaar(request.getAadhar());
        currentUser.setStatus(UserStatus.ACTIVE);
        userRepository.save(currentUser);
    }
}
