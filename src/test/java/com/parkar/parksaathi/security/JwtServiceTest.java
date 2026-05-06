package com.parkar.parksaathi.security;

import com.parkar.parksaathi.model.Users;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private String secretKey;
    private long accessTokenExpiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Generate a valid 256-bit secret key for HS256
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        secretKey = Base64.getEncoder().encodeToString(key.getEncoded());
        
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", accessTokenExpiration);
    }

    @Test
    void testGenerateAccessToken() {
        Users user = new Users();
        user.setPhone("1234567890");

        String token = jwtService.generateAccessToken(user);

        assertNotNull(token);
        assertEquals("1234567890", jwtService.extractPhone(token));
    }

    @Test
    void testExtractPhone() {
        Users user = new Users();
        user.setPhone("9876543210");
        String token = jwtService.generateAccessToken(user);

        String phone = jwtService.extractPhone(token);

        assertEquals("9876543210", phone);
    }

    @Test
    void testIsTokenValid_Success() {
        Users user = new Users();
        user.setPhone("1234567890");
        String token = jwtService.generateAccessToken(user);

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void testIsTokenValid_Expired() {
        // Set very short expiration
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1000L);
        Users user = new Users();
        user.setPhone("1234567890");
        String token = jwtService.generateAccessToken(user);

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void testIsTokenValid_InvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid.token.here"));
    }

    @Test
    void testGetAccessTokenExpiration() {
        assertEquals(accessTokenExpiration, jwtService.getAccessTokenExpiration());
    }
}
