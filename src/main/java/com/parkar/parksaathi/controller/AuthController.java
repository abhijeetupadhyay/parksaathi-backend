package com.parkar.parksaathi.controller;

import com.parkar.parksaathi.dto.*;
import com.parkar.parksaathi.service.AuthService;
import com.parkar.parksaathi.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
        log.atInfo().log("CONTROLLER: sendOtp");
        try {
            otpService.generateAndStoreOtp(request.getPhone());
            com.parkar.parksaathi.enums.UserStatus status = authService.getUserStatus(request.getPhone());
            SendOtpResponse sendOtpResponse = SendOtpResponse.builder().message("otp sent").expiresInSeconds(otpExpirationSeconds).userStatus(status.name()).build();
            return ResponseEntity.ok(sendOtpResponse);
        } catch (RuntimeException e) {
            log.atError().log("ERROR: sendOtp failed with exception: " + e);
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
        log.atInfo().log("CONTROLLER: verifyOtp");
        try {
            otpService.verifyOtp(request.getPhone(), request.getOtp());
            AuthResponse response = authService.signIn(request.getPhone());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.atError().log("ERROR: verifyOtp failed with exception: " + e);
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
        log.atInfo().log("CONTROLLER: refreshToken");
        try {
            AuthResponse response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.atError().log("ERROR: refreshToken failed with exception: " + e);
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
        log.atInfo().log("CONTROLLER: signOut");
        try {
            authService.signOut(request.getRefreshToken());
            APIResponse apiResponse = APIResponse.builder().message("Signed Out").build();
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.atError().log("ERROR: signOut failed with exception: " + e);
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
        log.atInfo().log("CONTROLLER: signup");
        try {
            authService.signup(request, currentUser);
            APIResponse apiResponse = APIResponse.builder().message("Signup successful, user status is now ACTIVE").build();
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.atError().log("ERROR: signup failed with exception: " + e);
            ApiErrorResponse error = ApiErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error("Bad Request")
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
