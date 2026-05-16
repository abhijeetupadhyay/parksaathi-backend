package com.parkar.parksaathi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkar.parksaathi.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthEntryPointTest {

    @Test
    void testCommence() throws IOException {
        AuthEntryPoint entryPoint = new AuthEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException authException = new BadCredentialsException("Bad credentials");

        entryPoint.commence(request, response, authException);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
        
        String body = response.getContentAsString();
        assertTrue(body.contains("Unauthorized"));
        assertTrue(body.contains("Authentication is required to access this resource"));
    }
}
