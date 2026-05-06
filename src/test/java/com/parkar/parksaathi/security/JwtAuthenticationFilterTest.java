package com.parkar.parksaathi.security;

import com.parkar.parksaathi.model.Users;
import com.parkar.parksaathi.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static com.parkar.parksaathi.constant.Constants.X_PARKSAATHI_AUTHORIZATION;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_NoAuthHeader() throws ServletException, IOException {
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_InvalidJwt() throws ServletException, IOException {
        String jwt = "invalid-jwt";
        request.addHeader(X_PARKSAATHI_AUTHORIZATION, "Bearer " + jwt);
        when(jwtService.isTokenValid(jwt)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_ValidJwt_Success() throws ServletException, IOException {
        String jwt = "valid-jwt";
        String phone = "1234567890";
        request.addHeader(X_PARKSAATHI_AUTHORIZATION, "Bearer " + jwt);

        Users user = new Users();
        user.setPhone(phone);

        when(jwtService.isTokenValid(jwt)).thenReturn(true);
        when(jwtService.extractPhone(jwt)).thenReturn(phone);
        when(userRepository.findByPhone(phone)).thenReturn(Optional.of(user));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(user, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void testDoFilterInternal_ValidJwt_UserNotFound() throws ServletException, IOException {
        String jwt = "valid-jwt";
        String phone = "1234567890";
        request.addHeader(X_PARKSAATHI_AUTHORIZATION, "Bearer " + jwt);

        when(jwtService.isTokenValid(jwt)).thenReturn(true);
        when(jwtService.extractPhone(jwt)).thenReturn(phone);
        when(userRepository.findByPhone(phone)).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
