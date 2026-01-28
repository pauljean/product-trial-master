package com.alten.producttrial.service;

import com.alten.producttrial.dto.LoginRequest;
import com.alten.producttrial.dto.LoginResponse;
import com.alten.producttrial.dto.RegisterRequest;
import com.alten.producttrial.exception.DuplicateResourceException;
import com.alten.producttrial.model.User;
import com.alten.producttrial.repository.UserRepository;
import com.alten.producttrial.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @InjectMocks
    private AuthService authService;
    
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    
    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setFirstname("Test");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
        
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setFirstname("Test");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
    }
    
    @Test
    void register_ValidRequest_ShouldCreateUser() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When
        User result = authService.register(registerRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByUsername("testuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void register_DuplicateEmail_ShouldThrowException() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        // When & Then
        assertThrows(DuplicateResourceException.class, () -> {
            authService.register(registerRequest);
        });
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void register_DuplicateUsername_ShouldThrowException() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // When & Then
        assertThrows(DuplicateResourceException.class, () -> {
            authService.register(registerRequest);
        });
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void login_ValidCredentials_ShouldReturnToken() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwt-token");
        
        // When
        LoginResponse result = authService.login(loginRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken("test@example.com");
    }
    
    @Test
    void login_InvalidCredentials_ShouldThrowException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));
        
        // When & Then
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(anyString());
    }
}
