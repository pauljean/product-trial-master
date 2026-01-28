package com.alten.producttrial.util;

import com.alten.producttrial.model.User;
import com.alten.producttrial.repository.UserRepository;
import com.alten.producttrial.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TestUtils {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Crée un utilisateur de test et retourne son token JWT
     */
    public String createUserAndGetToken(String email, String password) {
        User user = new User();
        user.setUsername(email.split("@")[0]);
        user.setFirstname("Test");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        
        return jwtUtil.generateToken(email);
    }
    
    /**
     * Génère un token JWT pour un email donné
     */
    public String generateToken(String email) {
        return jwtUtil.generateToken(email);
    }
}
