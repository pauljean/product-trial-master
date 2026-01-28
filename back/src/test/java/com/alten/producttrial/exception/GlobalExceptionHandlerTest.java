package com.alten.producttrial.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {
    
    private GlobalExceptionHandler handler;
    
    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }
    
    @Test
    void handleResourceNotFoundException_ShouldReturn404() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Product", "id", 1L);
        
        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            handler.handleResourceNotFoundException(exception);
        
        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Product"));
        assertNotNull(response.getBody().getTimestamp());
    }
    
    @Test
    void handleDuplicateResourceException_ShouldReturn409() {
        // Given
        DuplicateResourceException exception = new DuplicateResourceException("Product", "code", "TEST-001");
        
        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            handler.handleDuplicateResourceException(exception);
        
        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Product"));
        assertNotNull(response.getBody().getTimestamp());
    }
    
    @Test
    void handleUnauthorizedAccessException_ShouldReturn403() {
        // Given
        UnauthorizedAccessException exception = new UnauthorizedAccessException("CartItem", "id", 1L);
        
        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            handler.handleUnauthorizedAccessException(exception);
        
        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("CartItem"));
        assertNotNull(response.getBody().getTimestamp());
    }
    
    @Test
    void handleAuthenticationException_BadCredentials_ShouldReturn401() {
        // Given
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");
        
        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            handler.handleAuthenticationException(exception);
        
        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Email ou mot de passe incorrect", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }
    
    @Test
    void handleValidationExceptions_ShouldReturn400() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("productRequest", "code", "Le code produit est obligatoire");
        List<org.springframework.validation.ObjectError> errors = new ArrayList<>();
        errors.add(fieldError);
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(errors);
        
        // When
        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(exception);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("errors"));
        assertTrue(response.getBody().containsKey("message"));
        assertTrue(response.getBody().containsKey("timestamp"));
    }
    
    @Test
    void handleGenericException_ShouldReturn500() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");
        
        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            handler.handleGenericException(exception);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Une erreur interne est survenue", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }
}
