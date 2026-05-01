package com.parkar.parksaathi.security;

import com.parkar.parksaathi.constant.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MandatoryHeaderFilterTest {

    private MandatoryHeaderFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new MandatoryHeaderFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
        MDC.clear();
    }

    @Test
    void testDoFilterInternal_Success() throws ServletException, IOException {
        String correlationId = UUID.randomUUID().toString();
        request.addHeader(Constants.HEADER_DEVICE, Constants.DEVICE_MOBILE);
        request.addHeader(Constants.HEADER_CORRELATION_ID, correlationId);
        request.addHeader(Constants.HEADER_VERSION, Constants.VERSION_1_0_0);
        request.setRequestURI("/api/test");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void testDoFilterInternal_MissingDevice() throws ServletException, IOException {
        request.addHeader(Constants.HEADER_CORRELATION_ID, UUID.randomUUID().toString());
        request.addHeader(Constants.HEADER_VERSION, Constants.VERSION_1_0_0);
        request.setRequestURI("/api/test");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid or missing " + Constants.HEADER_DEVICE));
    }

    @Test
    void testDoFilterInternal_InvalidCorrelationId() throws ServletException, IOException {
        request.addHeader(Constants.HEADER_DEVICE, Constants.DEVICE_MOBILE);
        request.addHeader(Constants.HEADER_CORRELATION_ID, "not-a-uuid");
        request.addHeader(Constants.HEADER_VERSION, Constants.VERSION_1_0_0);
        request.setRequestURI("/api/test");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("Must be a valid UUID"));
    }

    @Test
    void testDoFilterInternal_InvalidVersion() throws ServletException, IOException {
        request.addHeader(Constants.HEADER_DEVICE, Constants.DEVICE_MOBILE);
        request.addHeader(Constants.HEADER_CORRELATION_ID, UUID.randomUUID().toString());
        request.addHeader(Constants.HEADER_VERSION, "2.0.0");
        request.setRequestURI("/api/test");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("Must be '" + Constants.VERSION_1_0_0 + "'"));
    }

    @Test
    void testDoFilterInternal_ExcludedPath() throws ServletException, IOException {
        request.setRequestURI("/swagger-ui/index.html");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void testMdcPopulation() throws ServletException, IOException {
        String correlationId = UUID.randomUUID().toString();
        request.addHeader(Constants.HEADER_DEVICE, Constants.DEVICE_MOBILE);
        request.addHeader(Constants.HEADER_CORRELATION_ID, correlationId);
        request.addHeader(Constants.HEADER_VERSION, Constants.VERSION_1_0_0);
        request.setRequestURI("/api/test");

        // Use a real filter chain to verify MDC during filter execution
        FilterChain realChain = (req, res) -> {
            assertEquals(Constants.DEVICE_MOBILE, MDC.get(Constants.HEADER_DEVICE));
            assertEquals(correlationId, MDC.get(Constants.HEADER_CORRELATION_ID));
            assertEquals(Constants.VERSION_1_0_0, MDC.get(Constants.HEADER_VERSION));
        };

        filter.doFilterInternal(request, response, realChain);

        // MDC should be cleared after filter
        assertNull(MDC.get(Constants.HEADER_DEVICE));
    }
}
