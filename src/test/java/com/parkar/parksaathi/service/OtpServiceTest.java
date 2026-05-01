package com.parkar.parksaathi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private OtpService otpService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(otpService, "otpExpirationSeconds", 300);
        ReflectionTestUtils.setField(otpService, "otpLength", 6);
    }

    @Test
    void testGenerateAndStoreOtp_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        assertDoesNotThrow(() -> otpService.generateAndStoreOtp("1234567890"));

        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).set(eq("otp:1234567890"), anyString(), eq(300L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testGenerateAndStoreOtp_Exception() {
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis connection failed"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> otpService.generateAndStoreOtp("1234567890"));
        assertEquals("Failed to generate OTP. Please try again.", ex.getMessage());
    }

    @Test
    void testVerifyOtp_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:1234567890")).thenReturn("123456");

        boolean result = otpService.verifyOtp("1234567890", "123456");

        assertTrue(result);
        verify(redisTemplate, times(1)).delete("otp:1234567890");
    }

    @Test
    void testVerifyOtp_ExpiredOrNotFound() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:1234567890")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> otpService.verifyOtp("1234567890", "123456"));
        assertEquals("OTP has expired or was not generated. Please request a new OTP.", ex.getMessage());
    }

    @Test
    void testVerifyOtp_InvalidOtp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:1234567890")).thenReturn("654321");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> otpService.verifyOtp("1234567890", "123456"));
        assertEquals("Invalid OTP. Please try again.", ex.getMessage());
    }

    @Test
    void testVerifyOtp_Exception() {
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> otpService.verifyOtp("1234567890", "123456"));
        assertEquals("OTP verification failed. Please try again.", ex.getMessage());
    }
}
