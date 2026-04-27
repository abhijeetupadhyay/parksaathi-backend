package com.parkar.parksaathi.controller;

import com.parkar.parksaathi.dto.*;
import com.parkar.parksaathi.service.AuthService;
import com.parkar.parksaathi.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.parkar.parksaathi.constant.Constants.*;

@RestController
@RequestMapping(AUTH)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    @Value("${otp.expiration-seconds}")
    private int otpExpirationSeconds;

    @PostMapping(V1_SEND_OTP)
    public ResponseEntity<?> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        try {
            otpService.generateAndStoreOtp(request.getPhone());
            com.parkar.parksaathi.enums.UserStatus status = authService.getUserStatus(request.getPhone());
            return ResponseEntity.ok(Map.of(
                    "message", "OTP sent",
                    "expiresInSeconds", otpExpirationSeconds,
                    "status", status
            ));
        } catch (RuntimeException e) {
            ApiErrorResponse error = ApiErrorResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .error("Internal Server Error")
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping(V1_VERIFY_OTP)
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            otpService.verifyOtp(request.getPhone(), request.getOtp());
            AuthResponse response = authService.signIn(request.getPhone());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            HttpStatus status = e.getMessage().contains("not found")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.UNAUTHORIZED;
            ApiErrorResponse error = ApiErrorResponse.builder()
                    .status(status.value())
                    .error(status.getReasonPhrase())
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(status).body(error);
        }
    }

    @PostMapping(V1_REFRESH)
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiErrorResponse error = ApiErrorResponse.builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .error("Unauthorized")
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping(V1_SIGNOUT)
    public ResponseEntity<?> signOut(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            authService.signOut(request.getRefreshToken());
            return ResponseEntity.ok(Map.of("message", "Signed out"));
        } catch (RuntimeException e) {
            ApiErrorResponse error = ApiErrorResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .error("Internal Server Error")
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping(V1_SIGNUP)
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignupRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.parkar.parksaathi.model.Users currentUser) {
        try {
            authService.signup(request, currentUser);
            return ResponseEntity.ok(Map.of("message", "Signup successful, status is now ACTIVE"));
        } catch (RuntimeException e) {
            ApiErrorResponse error = ApiErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error("Bad Request")
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
