package com.parkar.parksaathi.controller;

import com.parkar.parksaathi.dto.request.UpdateUserRequest;
import com.parkar.parksaathi.dto.request.UpdateUserStatusRequest;
import com.parkar.parksaathi.dto.response.UserResponse;
import com.parkar.parksaathi.model.Users;
import com.parkar.parksaathi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.parkar.parksaathi.constant.Constants.*;

@RestController
@RequestMapping(USER)
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PutMapping(VERSION1 + UPDATE_USER_ENDPOINT)
    public ResponseEntity<UserResponse> updateUser(
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal Users currentUser) {
        log.info("Update user request received for user ID: {}", currentUser.getId());
        UserResponse response = userService.updateUser(currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(VERSION1 + UPDATE_USER_STATUS_ENDPOINT)
    public ResponseEntity<UserResponse> updateUserStatus(
            @Valid @RequestBody UpdateUserStatusRequest request,
            @AuthenticationPrincipal Users currentUser) {
        log.info("Update user status request received for user ID: {}", currentUser.getId());
        UserResponse response = userService.updateUserStatus(currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }
}
