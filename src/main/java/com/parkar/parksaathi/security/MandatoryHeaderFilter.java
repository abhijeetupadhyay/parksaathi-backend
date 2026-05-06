package com.parkar.parksaathi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkar.parksaathi.constant.Constants;
import com.parkar.parksaathi.dto.response.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class MandatoryHeaderFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (isExcluded(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String device = request.getHeader(Constants.HEADER_DEVICE);
        String correlationId = request.getHeader(Constants.HEADER_CORRELATION_ID);
        String version = request.getHeader(Constants.HEADER_VERSION);

        if (device == null || (!device.equals(Constants.DEVICE_MOBILE) && !device.equals(Constants.DEVICE_WEB))) {
            sendError(response, "Invalid or missing " + Constants.HEADER_DEVICE + ". Must be 'mobile' or 'web'.");
            return;
        }

        if (correlationId == null || !isValidUUID(correlationId)) {
            sendError(response, "Invalid or missing " + Constants.HEADER_CORRELATION_ID + ". Must be a valid UUID.");
            return;
        }

        if (version == null || !version.equals(Constants.VERSION_1_0_0)) {
            sendError(response, "Invalid or missing " + Constants.HEADER_VERSION + ". Must be '" + Constants.VERSION_1_0_0 + "'.");
            return;
        }

        try {
            MDC.put(Constants.HEADER_DEVICE, device);
            MDC.put(Constants.HEADER_CORRELATION_ID, correlationId);
            MDC.put(Constants.HEADER_VERSION, version);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private boolean isExcluded(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isValidUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpServletResponse.SC_BAD_REQUEST)
                .error("Bad Request")
                .message(message)
                .build();

        objectMapper.findAndRegisterModules();
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
