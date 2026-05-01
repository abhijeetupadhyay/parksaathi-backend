package com.parkar.parksaathi.exception;

import com.parkar.parksaathi.dto.response.ApiErrorResponse;
import com.parkar.parksaathi.exception.customexceptions.AppException;
import com.parkar.parksaathi.exception.customexceptions.ResourceNotFoundException;
import com.parkar.parksaathi.exception.customexceptions.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User not found");
        ResponseEntity<ApiErrorResponse> response = handler.handleResourceNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().getMessage());
        assertEquals("Not Found", response.getBody().getError());
    }

    @Test
    void testHandleAppException() {
        AppException ex = new AppException("Bad request occurred");
        ResponseEntity<ApiErrorResponse> response = handler.handleAppException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad request occurred", response.getBody().getMessage());
        assertEquals("Bad Request", response.getBody().getError());
    }

    @Test
    void testHandleUnauthorizedException() {
        UnauthorizedException ex = new UnauthorizedException("Unauthorized access");
        ResponseEntity<ApiErrorResponse> response = handler.handleUnauthorizedException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized access", response.getBody().getMessage());
        assertEquals("Unauthorized", response.getBody().getError());
    }

    @Test
    void testHandleRuntimeException() {
        RuntimeException ex = new RuntimeException("Unexpected runtime error");
        ResponseEntity<ApiErrorResponse> response = handler.handleRuntimeException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected runtime error", response.getBody().getMessage());
        assertEquals("Internal Server Error", response.getBody().getError());
    }

    @Test
    void testHandleValidationException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be null");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ApiErrorResponse> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("field: must not be null"));
    }

    @Test
    void testHandleException() {
        Exception ex = new Exception("General error");
        ResponseEntity<ApiErrorResponse> response = handler.handleException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}
