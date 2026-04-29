package com.parkar.parksaathi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final String OTP_KEY_PREFIX = "otp:";

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${otp.expiration-seconds}")
    private int otpExpirationSeconds;

    @Value("${otp.length}")
    private int otpLength;

    /**
     * Generates a random OTP, stores it in Memcached with TTL, and logs it.
     * Replace the log statement with an SMS provider call (Twilio, MSG91, etc.) in production.
     */
    public void generateAndStoreOtp(String phone) {
        log.atInfo().log("SERVICE: generateAndStoreOtp");
        String otp = generateOtp();
        String key = OTP_KEY_PREFIX + phone;

        try {
            // Set OTP in Redis with expiration
            redisTemplate.opsForValue().set(key, otp, otpExpirationSeconds, TimeUnit.SECONDS);
            log.info("OTP generated for phone {}: {}", phone, otp);

            // TODO: Send OTP via SMS provider
            // smsService.sendSms(phone, "Your ParkSaathi OTP is: " + otp);
        } catch (Exception e) {
            log.error("Failed to store OTP in Redis for phone: {}", phone, e);
            throw new RuntimeException("Failed to generate OTP. Please try again.");
        }
    }

    /**
     * Verifies the OTP against the value stored in Redis.
     * Deletes the OTP on successful verification (single-use).
     */
    public boolean verifyOtp(String phone, String otp) {
        log.atInfo().log("SERVICE: verifyOtp");
        String key = OTP_KEY_PREFIX + phone;

        try {
            String storedOtp = redisTemplate.opsForValue().get(key);

            if (storedOtp == null) {
                throw new RuntimeException("OTP has expired or was not generated. Please request a new OTP.");
            }

            if (!storedOtp.equals(otp)) {
                throw new RuntimeException("Invalid OTP. Please try again.");
            }

            // OTP verified — delete it (single-use)
            redisTemplate.delete(key);
            return true;

        } catch (Exception e) {
            log.error("Failed to verify OTP from Redis for phone: {}", phone, e);
            throw new RuntimeException("OTP verification failed. Please try again.");
        }
    }

    private String generateOtp() {
        log.atInfo().log("SERVICE: generateOtp");
        int bound = (int) Math.pow(10, otpLength);
        int otp = secureRandom.nextInt(bound);
        return String.format("%0" + otpLength + "d", otp);
    }
}
