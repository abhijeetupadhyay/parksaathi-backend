package com.parkar.parksaathi.controller;

import com.parkar.parksaathi.dto.request.RefreshTokenRequest;
import com.parkar.parksaathi.dto.request.SendOtpRequest;
import com.parkar.parksaathi.dto.request.SignupRequest;
import com.parkar.parksaathi.dto.request.VerifyOtpRequest;
import com.parkar.parksaathi.dto.response.APIResponse;
import com.parkar.parksaathi.dto.response.AuthResponse;
import com.parkar.parksaathi.dto.response.SendOtpResponse;
import com.parkar.parksaathi.service.AuthService;
import com.parkar.parksaathi.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.parkar.parksaathi.constant.Constants.*;

@RestController
@RequestMapping(AUTH)
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    @Value("${otp.expiration-seconds}")
    private int otpExpirationSeconds;

    @PostMapping(V1_SEND_OTP)
    public ResponseEntity<?> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        otpService.generateAndStoreOtp(request.getPhone());
        com.parkar.parksaathi.enums.UserStatus status = authService.getUserStatus(request.getPhone());
        SendOtpResponse sendOtpResponse = SendOtpResponse.builder()
                .message("otp sent")
                .expiresInSeconds(otpExpirationSeconds)
                .userStatus(status.name())
                .build();
        return ResponseEntity.ok(sendOtpResponse);
    }

    @PostMapping(V1_VERIFY_OTP)
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        otpService.verifyOtp(request.getPhone(), request.getOtp());
        AuthResponse response = authService.signIn(request.getPhone());
        return ResponseEntity.ok(response);
    }

    @PostMapping(V1_REFRESH)
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping(V1_SIGNOUT)
    public ResponseEntity<?> signOut(@Valid @RequestBody RefreshTokenRequest request) {
        authService.signOut(request.getRefreshToken());
        APIResponse apiResponse = APIResponse.builder().message("Signed Out").build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping(V1_SIGNUP)
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignupRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.parkar.parksaathi.model.Users currentUser) {
        authService.signup(request, currentUser);
        APIResponse apiResponse = APIResponse.builder().message("Signup successful, user status is now ACTIVE").build();
        return ResponseEntity.ok(apiResponse);
    }
}
