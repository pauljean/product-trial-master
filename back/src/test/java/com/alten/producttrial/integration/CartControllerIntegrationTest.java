package com.alten.producttrial.integration;

import com.alten.producttrial.dto.CartItemRequest;
import com.alten.producttrial.model.CartItem;
import com.alten.producttrial.model.Product;
import com.alten.producttrial.model.User;
import com.alten.producttrial.repository.CartItemRepository;
import com.alten.producttrial.repository.ProductRepository;
import com.alten.producttrial.repository.UserRepository;
import com.alten.producttrial.security.JwtUtil;
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

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CartControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private User testUser;
    private Product testProduct;
    private CartItem testCartItem;
    private String authToken;
    
    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        
        // Créer un utilisateur
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setFirstname("Test");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);
        
        // Créer un produit
        testProduct = new Product();
        testProduct.setCode("PROD-001");
        testProduct.setName("Test Product");
        testProduct.setCategory("Electronics");
        testProduct.setPrice(99.99);
        testProduct.setQuantity(10);
        testProduct.setInventoryStatus(Product.InventoryStatus.INSTOCK);
        testProduct = productRepository.save(testProduct);
        
        // Créer un item de panier
        testCartItem = new CartItem();
        testCartItem.setUser(testUser);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        testCartItem = cartItemRepository.save(testCartItem);
        
        // Générer un token JWT
        authToken = jwtUtil.generateToken(testUser.getEmail());
    }
    
    @Test
    void getCartItems_Authenticated_ShouldReturnCartItems() throws Exception {
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", isA(java.util.List.class)))
            .andExpect(jsonPath("$[0].id", is(testCartItem.getId().intValue())))
            .andExpect(jsonPath("$[0].quantity", is(2)));
    }
    
    @Test
    void getCartItems_Unauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/cart"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    void addToCart_ValidRequest_ShouldAddItem() throws Exception {
        Product newProduct = new Product();
        newProduct.setCode("PROD-002");
        newProduct.setName("New Product");
        newProduct.setCategory("Electronics");
        newProduct.setPrice(49.99);
        newProduct.setQuantity(5);
        newProduct.setInventoryStatus(Product.InventoryStatus.INSTOCK);
        newProduct = productRepository.save(newProduct);
        
        CartItemRequest request = new CartItemRequest();
        request.setProductId(newProduct.getId());
        request.setQuantity(3);
        
        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity", is(3)))
            .andExpect(jsonPath("$.product.id", is(newProduct.getId().intValue())));
    }
    
    @Test
    void addToCart_ExistingItem_ShouldUpdateQuantity() throws Exception {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(testProduct.getId());
        request.setQuantity(1);
        
        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity", is(3))); // 2 + 1
    }
    
    @Test
    void addToCart_InvalidProduct_ShouldReturn404() throws Exception {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(999L);
        request.setQuantity(1);
        
        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }
    
    @Test
    void updateCartItemQuantity_ValidRequest_ShouldUpdate() throws Exception {
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("quantity", 5);
        
        mockMvc.perform(patch("/api/cart/{cartItemId}", testCartItem.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity", is(5)));
    }
    
    @Test
    void updateCartItemQuantity_DifferentUser_ShouldReturn403() throws Exception {
        // Créer un autre utilisateur
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setFirstname("Other");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword(passwordEncoder.encode("password123"));
        otherUser = userRepository.save(otherUser);
        String otherToken = jwtUtil.generateToken(otherUser.getEmail());
        
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("quantity", 5);
        
        mockMvc.perform(patch("/api/cart/{cartItemId}", testCartItem.getId())
                .header("Authorization", "Bearer " + otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isForbidden());
    }
    
    @Test
    void removeFromCart_ValidRequest_ShouldDelete() throws Exception {
        mockMvc.perform(delete("/api/cart/{cartItemId}", testCartItem.getId())
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isNoContent());
        
        // Vérifier que l'item a été supprimé
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }
    
    @Test
    void clearCart_ValidRequest_ShouldDeleteAll() throws Exception {
        mockMvc.perform(delete("/api/cart")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isNoContent());
        
        // Vérifier que le panier est vide
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }
}
