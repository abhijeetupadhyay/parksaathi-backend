package com.parkar.parksaathi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkar.parksaathi.dto.request.UpdateUserRequest;
import com.parkar.parksaathi.dto.request.UpdateUserStatusRequest;
import com.parkar.parksaathi.dto.response.UserResponse;
import com.parkar.parksaathi.enums.UserStatus;
import com.parkar.parksaathi.exception.customexceptions.ResourceNotFoundException;
import com.parkar.parksaathi.repository.UserRepository;
import com.parkar.parksaathi.security.JwtService;
import com.parkar.parksaathi.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.parkar.parksaathi.constant.Constants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== updateUser tests ====================

    @Test
    void testUpdateUser_Success() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .aadhaar("999988887777")
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .name("Jane Doe")
                .phone("9876543210")
                .email("jane@example.com")
                .aadhaar("999988887777")
                .status(UserStatus.ACTIVE)
                .build();

        when(userService.updateUser(any(), any(UpdateUserRequest.class))).thenReturn(response);

        mockMvc.perform(put(USER + VERSION1 + UPDATE_USER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.phone").value("9876543210"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.aadhaar").value("999988887777"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void testUpdateUser_PartialUpdate() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Updated Name Only")
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .name("Updated Name Only")
                .phone("9876543210")
                .email("john@example.com")
                .aadhaar("123456789012")
                .status(UserStatus.ACTIVE)
                .build();

        when(userService.updateUser(any(), any(UpdateUserRequest.class))).thenReturn(response);

        mockMvc.perform(put(USER + VERSION1 + UPDATE_USER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name Only"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void testUpdateUser_UserNotFound() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Nonexistent")
                .build();

        when(userService.updateUser(any(), any(UpdateUserRequest.class)))
                .thenThrow(new ResourceNotFoundException("User not found with id: 99"));

        mockMvc.perform(put(USER + VERSION1 + UPDATE_USER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 99"));
    }

    @Test
    void testUpdateUser_InvalidEmailFormat() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("not-an-email")
                .build();

        mockMvc.perform(put(USER + VERSION1 + UPDATE_USER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateUser_EmptyBody() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1L)
                .name("John Doe")
                .phone("9876543210")
                .email("john@example.com")
                .aadhaar("123456789012")
                .status(UserStatus.ACTIVE)
                .build();

        when(userService.updateUser(any(), any(UpdateUserRequest.class))).thenReturn(response);

        mockMvc.perform(put(USER + VERSION1 + UPDATE_USER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testUpdateUser_ServiceThrowsRuntimeException() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Error Case")
                .build();

        when(userService.updateUser(any(), any(UpdateUserRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(put(USER + VERSION1 + UPDATE_USER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    // ==================== updateUserStatus tests ====================

    @Test
    void testUpdateUserStatus_Success() throws Exception {
        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .status(UserStatus.INACTIVE)
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .name("John Doe")
                .phone("9876543210")
                .email("john@example.com")
                .aadhaar("123456789012")
                .status(UserStatus.INACTIVE)
                .build();

        when(userService.updateUserStatus(any(), any(UpdateUserStatusRequest.class))).thenReturn(response);

        mockMvc.perform(patch(USER + VERSION1 + UPDATE_USER_STATUS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void testUpdateUserStatus_ToBlocked() throws Exception {
        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .status(UserStatus.BLOCKED)
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .name("John Doe")
                .phone("9876543210")
                .status(UserStatus.BLOCKED)
                .build();

        when(userService.updateUserStatus(any(), any(UpdateUserStatusRequest.class))).thenReturn(response);

        mockMvc.perform(patch(USER + VERSION1 + UPDATE_USER_STATUS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void testUpdateUserStatus_UserNotFound() throws Exception {
        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .status(UserStatus.ACTIVE)
                .build();

        when(userService.updateUserStatus(any(), any(UpdateUserStatusRequest.class)))
                .thenThrow(new ResourceNotFoundException("User not found with id: 99"));

        mockMvc.perform(patch(USER + VERSION1 + UPDATE_USER_STATUS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 99"));
    }

    @Test
    void testUpdateUserStatus_NullStatus() throws Exception {
        mockMvc.perform(patch(USER + VERSION1 + UPDATE_USER_STATUS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateUserStatus_MissingStatusField() throws Exception {
        mockMvc.perform(patch(USER + VERSION1 + UPDATE_USER_STATUS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateUserStatus_InvalidStatusValue() throws Exception {
        mockMvc.perform(patch(USER + VERSION1 + UPDATE_USER_STATUS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"UNKNOWN_STATUS\"}"))
                .andExpect(status().isInternalServerError());
    }
}
