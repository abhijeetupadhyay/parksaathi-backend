package com.parkar.parksaathi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkar.parksaathi.dto.request.RefreshTokenRequest;
import com.parkar.parksaathi.dto.request.SendOtpRequest;
import com.parkar.parksaathi.dto.request.SignupRequest;
import com.parkar.parksaathi.dto.request.VerifyOtpRequest;
import com.parkar.parksaathi.dto.response.AuthResponse;
import com.parkar.parksaathi.enums.UserStatus;
import com.parkar.parksaathi.exception.customexceptions.UnauthorizedException;
import com.parkar.parksaathi.repository.UserRepository;
import com.parkar.parksaathi.security.JwtService;
import com.parkar.parksaathi.service.AuthService;
import com.parkar.parksaathi.service.OtpService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private OtpService otpService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSendOtp_Success() throws Exception {
        SendOtpRequest request = new SendOtpRequest();
        request.setPhone("1234567890");

        doNothing().when(otpService).generateAndStoreOtp("1234567890");
        when(authService.getUserStatus("1234567890")).thenReturn(UserStatus.ACTIVE);

        mockMvc.perform(post(AUTH + V1_SEND_OTP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("otp sent"))
                .andExpect(jsonPath("$.userStatus").value("ACTIVE"));
    }

    @Test
    void testSendOtp_Failure() throws Exception {
        SendOtpRequest request = new SendOtpRequest();
        request.setPhone("1234567890");

        doThrow(new RuntimeException("OTP error")).when(otpService).generateAndStoreOtp(anyString());

        mockMvc.perform(post(AUTH + V1_SEND_OTP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("OTP error"));
    }

    @Test
    void testVerifyOtp_Success() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setPhone("1234567890");
        request.setOtp("123456");

        when(otpService.verifyOtp("1234567890", "123456")).thenReturn(true);
        
        AuthResponse response = AuthResponse.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .build();
        when(authService.signIn("1234567890")).thenReturn(response);

        mockMvc.perform(post(AUTH + V1_VERIFY_OTP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));
    }

    @Test
    void testVerifyOtp_Failure() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setPhone("1234567890");
        request.setOtp("123456");

        when(otpService.verifyOtp("1234567890", "123456")).thenThrow(new UnauthorizedException("Invalid OTP"));

        mockMvc.perform(post(AUTH + V1_VERIFY_OTP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid OTP"));
    }

    @Test
    void testRefreshToken_Success() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh");

        AuthResponse response = AuthResponse.builder().accessToken("new-access").build();
        when(authService.refreshToken("refresh")).thenReturn(response);

        mockMvc.perform(post(AUTH + V1_REFRESH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"));
    }

    @Test
    void testSignOut_Success() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh");

        doNothing().when(authService).signOut("refresh");

        mockMvc.perform(post(AUTH + V1_SIGNOUT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Signed Out"));
    }

    @Test
    void testSignup_Success() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setName("John");
        request.setEmail("john@example.com");
        request.setAadhar("1234");

        // The currentUser argument is injected via @AuthenticationPrincipal, 
        // mocking it directly in WebMvcTest with filters disabled implies it will be null.
        // We just ensure the controller handles it without crashing in our test scenario
        // or we can test the service invocation.
        doNothing().when(authService).signup(any(SignupRequest.class), any());

        mockMvc.perform(post(AUTH + V1_SIGNUP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Signup successful, user status is now ACTIVE"));
    }
}
