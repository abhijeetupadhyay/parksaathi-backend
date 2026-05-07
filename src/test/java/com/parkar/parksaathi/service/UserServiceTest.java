package com.parkar.parksaathi.service;

import com.parkar.parksaathi.dto.request.UpdateUserRequest;
import com.parkar.parksaathi.dto.request.UpdateUserStatusRequest;
import com.parkar.parksaathi.dto.response.UserResponse;
import com.parkar.parksaathi.enums.UserStatus;
import com.parkar.parksaathi.exception.customexceptions.ResourceNotFoundException;
import com.parkar.parksaathi.model.Users;
import com.parkar.parksaathi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private Users testUser;

    @BeforeEach
    void setUp() {
        testUser = new Users();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setPhone("9876543210");
        testUser.setEmail("john@example.com");
        testUser.setAadhaar("123456789012");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        testUser.setUpdatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    // ==================== updateUser tests ====================

    @Test
    void testUpdateUser_AllFields() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .aadhaar("999988887777")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUser(1L, request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Jane Doe", response.getName());
        assertEquals("9876543210", response.getPhone());
        assertEquals("jane@example.com", response.getEmail());
        assertEquals("999988887777", response.getAadhaar());
        assertEquals(UserStatus.ACTIVE, response.getStatus());

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(userRepository).save(userCaptor.capture());
        Users savedUser = userCaptor.getValue();
        assertEquals("Jane Doe", savedUser.getName());
        assertEquals("jane@example.com", savedUser.getEmail());
        assertEquals("999988887777", savedUser.getAadhaar());
        assertNotNull(savedUser.getUpdatedAt());
    }

    @Test
    void testUpdateUser_OnlyName() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Updated Name")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUser(1L, request);

        assertEquals("Updated Name", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("123456789012", response.getAadhaar());
    }

    @Test
    void testUpdateUser_OnlyEmail() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("newemail@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUser(1L, request);

        assertEquals("John Doe", response.getName());
        assertEquals("newemail@example.com", response.getEmail());
        assertEquals("123456789012", response.getAadhaar());
    }

    @Test
    void testUpdateUser_OnlyAadhaar() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .aadhaar("111122223333")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUser(1L, request);

        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("111122223333", response.getAadhaar());
    }

    @Test
    void testUpdateUser_NoFieldsProvided() {
        UpdateUserRequest request = UpdateUserRequest.builder().build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUser(1L, request);

        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("123456789012", response.getAadhaar());
        verify(userRepository).save(any(Users.class));
    }

    @Test
    void testUpdateUser_UserNotFound() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Irrelevant")
                .build();

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUser(99L, request)
        );

        assertEquals("User not found with id: 99", exception.getMessage());
        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    void testUpdateUser_UpdatesTimestamp() {
        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);

        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Timestamp Test")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateUser(1L, request);

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(userRepository).save(userCaptor.capture());
        assertTrue(userCaptor.getValue().getUpdatedAt().isAfter(beforeUpdate));
    }

    // ==================== updateUserStatus tests ====================

    @Test
    void testUpdateUserStatus_ActiveToInactive() {
        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .status(UserStatus.INACTIVE)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUserStatus(1L, request);

        assertNotNull(response);
        assertEquals(UserStatus.INACTIVE, response.getStatus());

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(UserStatus.INACTIVE, userCaptor.getValue().getStatus());
    }

    @Test
    void testUpdateUserStatus_PendingToActive() {
        testUser.setStatus(UserStatus.PENDING);

        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUserStatus(1L, request);

        assertEquals(UserStatus.ACTIVE, response.getStatus());
    }

    @Test
    void testUpdateUserStatus_ToBlocked() {
        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .status(UserStatus.BLOCKED)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUserStatus(1L, request);

        assertEquals(UserStatus.BLOCKED, response.getStatus());
    }

    @Test
    void testUpdateUserStatus_UserNotFound() {
        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUserStatus(99L, request)
        );

        assertEquals("User not found with id: 99", exception.getMessage());
        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    void testUpdateUserStatus_UpdatesTimestamp() {
        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);

        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .status(UserStatus.INACTIVE)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateUserStatus(1L, request);

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(userRepository).save(userCaptor.capture());
        assertTrue(userCaptor.getValue().getUpdatedAt().isAfter(beforeUpdate));
    }

    // ==================== Response mapping tests ====================

    @Test
    void testUpdateUser_ResponseMapsAllFields() {
        UpdateUserRequest request = UpdateUserRequest.builder().build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUser(1L, request);

        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getName(), response.getName());
        assertEquals(testUser.getPhone(), response.getPhone());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getAadhaar(), response.getAadhaar());
        assertEquals(testUser.getStatus(), response.getStatus());
    }
}
