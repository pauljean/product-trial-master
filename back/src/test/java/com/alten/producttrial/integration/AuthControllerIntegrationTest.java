package com.alten.producttrial.integration;

import com.alten.producttrial.dto.LoginRequest;
import com.alten.producttrial.dto.RegisterRequest;
import com.alten.producttrial.model.User;
import com.alten.producttrial.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setFirstname("Test");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(testUser);
    }
    
    @Test
    void register_ValidRequest_ShouldCreateUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setFirstname("New");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        
        mockMvc.perform(post("/api/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email", is("new@example.com")))
            .andExpect(jsonPath("$.username", is("newuser")))
            .andExpect(jsonPath("$.password").doesNotExist()); // Password should not be in response
    }
    
    @Test
    void register_DuplicateEmail_ShouldReturn409() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("anotheruser");
        request.setFirstname("Another");
        request.setEmail("test@example.com"); // Email déjà existant
        request.setPassword("password123");
        
        mockMvc.perform(post("/api/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message", containsString("email")));
    }
    
    @Test
    void register_DuplicateUsername_ShouldReturn409() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser"); // Username déjà existant
        request.setFirstname("Another");
        request.setEmail("another@example.com");
        request.setPassword("password123");
        
        mockMvc.perform(post("/api/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message", containsString("username")));
    }
    
    @Test
    void register_InvalidData_ShouldReturn400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        // Données invalides : email manquant
        
        mockMvc.perform(post("/api/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists());
    }
    
    @Test
    void login_ValidCredentials_ShouldReturnToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        
        mockMvc.perform(post("/api/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", notNullValue()))
            .andExpect(jsonPath("$.token", not(emptyString())));
    }
    
    @Test
    void login_InvalidCredentials_ShouldReturn401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");
        
        mockMvc.perform(post("/api/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message", is("Email ou mot de passe incorrect")))
            .andExpect(jsonPath("$.status", is(401)));
    }
    
    @Test
    void login_NonExistingUser_ShouldReturn401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");
        
        mockMvc.perform(post("/api/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message", is("Email ou mot de passe incorrect")));
    }
    
    @Test
    void login_InvalidData_ShouldReturn400() throws Exception {
        LoginRequest request = new LoginRequest();
        // Données invalides : email manquant
        
        mockMvc.perform(post("/api/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists());
    }
}
