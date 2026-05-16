package com.parkar.parksaathi.service;

import com.parkar.parksaathi.dto.request.UpdateUserRequest;
import com.parkar.parksaathi.dto.request.UpdateUserStatusRequest;
import com.parkar.parksaathi.dto.response.UserResponse;
import com.parkar.parksaathi.enums.UserStatus;
import com.parkar.parksaathi.exception.customexceptions.ResourceNotFoundException;
import com.parkar.parksaathi.model.Users;
import com.parkar.parksaathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getAadhaar() != null) {
            user.setAadhaar(request.getAadhaar());
        }

        user.setUpdatedAt(LocalDateTime.now());
        Users savedUser = userRepository.save(user);

        log.info("User {} details updated successfully", userId);

        return buildUserResponse(savedUser);
    }

    public UserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserStatus previousStatus = user.getStatus();
        user.setStatus(request.getStatus());
        user.setUpdatedAt(LocalDateTime.now());
        Users savedUser = userRepository.save(user);

        log.info("User {} status updated from {} to {}", userId, previousStatus, request.getStatus());

        return buildUserResponse(savedUser);
    }

    private UserResponse buildUserResponse(Users user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .aadhaar(user.getAadhaar())
                .status(user.getStatus())
                .build();
    }
}
