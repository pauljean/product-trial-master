package com.alten.producttrial.integration;

import com.alten.producttrial.dto.WishlistItemRequest;
import com.alten.producttrial.model.Product;
import com.alten.producttrial.model.User;
import com.alten.producttrial.model.WishlistItem;
import com.alten.producttrial.repository.ProductRepository;
import com.alten.producttrial.repository.UserRepository;
import com.alten.producttrial.repository.WishlistItemRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WishlistControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private WishlistItemRepository wishlistItemRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private User testUser;
    private Product testProduct;
    private WishlistItem testWishlistItem;
    private String authToken;
    
    @BeforeEach
    void setUp() {
        wishlistItemRepository.deleteAll();
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
        
        // Créer un item de wishlist
        testWishlistItem = new WishlistItem();
        testWishlistItem.setUser(testUser);
        testWishlistItem.setProduct(testProduct);
        testWishlistItem = wishlistItemRepository.save(testWishlistItem);
        
        // Générer un token JWT
        authToken = jwtUtil.generateToken(testUser.getEmail());
    }
    
    @Test
    void getWishlistItems_Authenticated_ShouldReturnItems() throws Exception {
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", isA(java.util.List.class)))
            .andExpect(jsonPath("$[0].id", is(testWishlistItem.getId().intValue())))
            .andExpect(jsonPath("$[0].product.id", is(testProduct.getId().intValue())));
    }
    
    @Test
    void getWishlistItems_Unauthenticated_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/wishlist"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    void addToWishlist_NewProduct_ShouldAddItem() throws Exception {
        Product newProduct = new Product();
        newProduct.setCode("PROD-002");
        newProduct.setName("New Product");
        newProduct.setCategory("Electronics");
        newProduct.setPrice(49.99);
        newProduct.setQuantity(5);
        newProduct.setInventoryStatus(Product.InventoryStatus.INSTOCK);
        newProduct = productRepository.save(newProduct);
        
        WishlistItemRequest request = new WishlistItemRequest();
        request.setProductId(newProduct.getId());
        
        mockMvc.perform(post("/api/wishlist/add")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.product.id", is(newProduct.getId().intValue())));
    }
    
    @Test
    void addToWishlist_ExistingProduct_ShouldReturnExisting() throws Exception {
        WishlistItemRequest request = new WishlistItemRequest();
        request.setProductId(testProduct.getId());
        
        mockMvc.perform(post("/api/wishlist/add")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(testWishlistItem.getId().intValue())));
    }
    
    @Test
    void addToWishlist_InvalidProduct_ShouldReturn404() throws Exception {
        WishlistItemRequest request = new WishlistItemRequest();
        request.setProductId(999L);
        
        mockMvc.perform(post("/api/wishlist/add")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }
    
    @Test
    void removeFromWishlist_ValidRequest_ShouldDelete() throws Exception {
        mockMvc.perform(delete("/api/wishlist/{wishlistItemId}", testWishlistItem.getId())
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isNoContent());
        
        // Vérifier que l'item a été supprimé
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }
    
    @Test
    void removeFromWishlist_DifferentUser_ShouldReturn403() throws Exception {
        // Créer un autre utilisateur
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setFirstname("Other");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword(passwordEncoder.encode("password123"));
        otherUser = userRepository.save(otherUser);
        String otherToken = jwtUtil.generateToken(otherUser.getEmail());
        
        mockMvc.perform(delete("/api/wishlist/{wishlistItemId}", testWishlistItem.getId())
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isForbidden());
    }
    
    @Test
    void removeFromWishlist_NonExistingItem_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/wishlist/999")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isNotFound());
    }
}
