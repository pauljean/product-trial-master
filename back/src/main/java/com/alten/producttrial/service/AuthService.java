package com.alten.producttrial.service;

import com.alten.producttrial.dto.LoginRequest;
import com.alten.producttrial.dto.LoginResponse;
import com.alten.producttrial.dto.RegisterRequest;
import com.alten.producttrial.exception.DuplicateResourceException;
import com.alten.producttrial.model.User;
import com.alten.producttrial.repository.UserRepository;
import com.alten.producttrial.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    
    public AuthService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder, 
                      JwtUtil jwtUtil, 
                      AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }
    
    @Transactional
    public User register(RegisterRequest request) {
        log.info("Tentative d'enregistrement pour l'email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Tentative d'enregistrement avec un email existant: {}", request.getEmail());
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Tentative d'enregistrement avec un nom d'utilisateur existant: {}", request.getUsername());
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setFirstname(request.getFirstname());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        User savedUser = userRepository.save(user);
        log.info("Utilisateur créé avec succès - ID: {}, Email: {}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }
    
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        String token = jwtUtil.generateToken(request.getEmail());
        return new LoginResponse(token);
    }
}
