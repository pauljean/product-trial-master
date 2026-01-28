package com.alten.producttrial.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "jwt.secret=testSecretKeyForJWTTokenGenerationThatShouldBeAtLeast256BitsLong",
    "jwt.expiration=86400000"
})
class JwtUtilTest {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private String testEmail;
    
    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
    }
    
    @Test
    void generateToken_ValidEmail_ShouldGenerateToken() {
        // When
        String token = jwtUtil.generateToken(testEmail);
        
        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT a 3 parties
    }
    
    @Test
    void extractEmail_ValidToken_ShouldExtractEmail() {
        // Given
        String token = jwtUtil.generateToken(testEmail);
        
        // When
        String extractedEmail = jwtUtil.extractEmail(token);
        
        // Then
        assertEquals(testEmail, extractedEmail);
    }
    
    @Test
    void validateToken_ValidToken_ShouldReturnTrue() {
        // Given
        String token = jwtUtil.generateToken(testEmail);
        
        // When
        Boolean isValid = jwtUtil.validateToken(token, testEmail);
        
        // Then
        assertTrue(isValid);
    }
    
    @Test
    void validateToken_DifferentEmail_ShouldReturnFalse() {
        // Given
        String token = jwtUtil.generateToken(testEmail);
        String differentEmail = "other@example.com";
        
        // When
        Boolean isValid = jwtUtil.validateToken(token, differentEmail);
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    void extractExpiration_ValidToken_ShouldReturnFutureDate() {
        // Given
        String token = jwtUtil.generateToken(testEmail);
        
        // When
        Date expiration = jwtUtil.extractExpiration(token);
        
        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }
    
    @Test
    void generateToken_DifferentEmails_ShouldGenerateDifferentTokens() {
        // When
        String token1 = jwtUtil.generateToken("user1@example.com");
        String token2 = jwtUtil.generateToken("user2@example.com");
        
        // Then
        assertNotEquals(token1, token2);
    }
}
