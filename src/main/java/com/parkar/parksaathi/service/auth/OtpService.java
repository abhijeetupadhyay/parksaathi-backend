package com.parkar.parksaathi.service.auth;

import com.parkar.parksaathi.exception.customexceptions.AppException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
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
        String otp = generateOtp();
        String key = OTP_KEY_PREFIX + phone;

        try {
            // Set OTP in Redis with expiration
            redisTemplate.opsForValue().set(key, otp, otpExpirationSeconds, TimeUnit.SECONDS);
            log.info("OTP generated and stored for phone {}", phone);

            // TODO: Send OTP via SMS provider
            // smsService.sendSms(phone, "Your ParkSaathi OTP is: " + otp);
        } catch (Exception e) {
            log.error("Failed to store OTP in Redis for phone: {}", phone, e);
            throw new AppException("Failed to generate OTP. Please try again.");
        }
    }

    /**
     * Verifies the OTP against the value stored in Redis.
     * Deletes the OTP on successful verification (single-use).
     */
    public boolean verifyOtp(String phone, String otp) {
        String key = OTP_KEY_PREFIX + phone;

        try {
            String storedOtp = redisTemplate.opsForValue().get(key);

            if (storedOtp == null) {
                log.warn("OTP verification failed: OTP expired or not found for phone {}", phone);
                throw new AppException("OTP has expired or was not generated. Please request a new OTP.");
            }

            if (!storedOtp.equals(otp)) {
                log.warn("OTP verification failed: Invalid OTP for phone {}", phone);
                throw new AppException("Invalid OTP. Please try again.");
            }

            // OTP verified — delete it (single-use)
            redisTemplate.delete(key);
            log.info("OTP verified and deleted for phone {}", phone);
            return true;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to verify OTP from Redis for phone: {}", phone, e);
            throw new AppException("OTP verification failed. Please try again.");
        }
    }

    private String generateOtp() {
        int bound = (int) Math.pow(10, otpLength);
        int otp = secureRandom.nextInt(bound);
        return String.format("%0" + otpLength + "d", otp);
    }
}
