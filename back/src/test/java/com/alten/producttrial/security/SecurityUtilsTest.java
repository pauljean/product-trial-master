package com.alten.producttrial.security;

import com.alten.producttrial.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SecurityUtilsTest {
    
    @Mock
    private AppProperties appProperties;
    
    @Mock
    private AppProperties.Admin admin;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private SecurityUtils securityUtils;
    
    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        lenient().when(appProperties.getAdmin()).thenReturn(admin);
    }
    
    @Test
    void getCurrentUserEmail_AuthenticatedUser_ShouldReturnEmail() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("user@example.com");
        when(authentication.getName()).thenReturn("user@example.com");
        
        // When
        String result = securityUtils.getCurrentUserEmail();
        
        // Then
        assertEquals("user@example.com", result);
    }
    
    @Test
    void getCurrentUserEmail_NotAuthenticated_ShouldReturnNull() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);
        
        // When
        String result = securityUtils.getCurrentUserEmail();
        
        // Then
        assertNull(result);
    }
    
    @Test
    void getCurrentUserEmail_AnonymousUser_ShouldReturnNull() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        
        // When
        String result = securityUtils.getCurrentUserEmail();
        
        // Then
        assertNull(result);
    }
    
    @Test
    void isAdmin_AdminEmail_ShouldReturnTrue() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("admin@admin.com");
        when(authentication.getName()).thenReturn("admin@admin.com");
        when(admin.getEmail()).thenReturn("admin@admin.com");
        
        // When
        boolean result = securityUtils.isAdmin();
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void isAdmin_RegularUser_ShouldReturnFalse() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("user@example.com");
        when(authentication.getName()).thenReturn("user@example.com");
        when(admin.getEmail()).thenReturn("admin@admin.com");
        
        // When
        boolean result = securityUtils.isAdmin();
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void isAdmin_NotAuthenticated_ShouldReturnFalse() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);
        
        // When
        boolean result = securityUtils.isAdmin();
        
        // Then
        assertFalse(result);
    }
}
