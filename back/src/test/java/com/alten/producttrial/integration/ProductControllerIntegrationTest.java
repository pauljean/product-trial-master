package com.alten.producttrial.integration;

import com.alten.producttrial.dto.ProductRequest;
import com.alten.producttrial.model.Product;
import com.alten.producttrial.model.User;
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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private Product testProduct;
    private User adminUser;
    private String adminToken;
    
    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        userRepository.deleteAll();
        
        // Créer un utilisateur admin
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setFirstname("Admin");
        adminUser.setEmail("admin@admin.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser = userRepository.save(adminUser);
        adminToken = jwtUtil.generateToken(adminUser.getEmail());
        
        // Créer un utilisateur normal
        User regularUser = new User();
        regularUser.setUsername("user");
        regularUser.setFirstname("User");
        regularUser.setEmail("user@example.com");
        regularUser.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(regularUser);
        
        testProduct = new Product();
        testProduct.setCode("TEST-001");
        testProduct.setName("Test Product");
        testProduct.setCategory("Electronics");
        testProduct.setPrice(99.99);
        testProduct.setQuantity(10);
        testProduct.setInventoryStatus(Product.InventoryStatus.INSTOCK);
        testProduct = productRepository.save(testProduct);
    }
    
    @Test
    void getAllProducts_ShouldReturnProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", isA(List.class)))
            .andExpect(jsonPath("$[0].code", is("TEST-001")));
    }
    
    @Test
    void getAllProducts_WithPagination_ShouldReturnPage() throws Exception {
        mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", isA(List.class)))
            .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }
    
    @Test
    void getProductById_ExistingId_ShouldReturnProduct() throws Exception {
        mockMvc.perform(get("/api/products/{id}", testProduct.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(testProduct.getId().intValue())))
            .andExpect(jsonPath("$.code", is("TEST-001")));
    }
    
    @Test
    void getProductById_NonExistingId_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/products/999"))
            .andExpect(status().isNotFound());
    }
    
    @Test
    void createProduct_AsAdmin_ShouldCreateProduct() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setCode("NEW-001");
        request.setName("New Product");
        request.setDescription("New Description");
        request.setCategory("Electronics");
        request.setPrice(49.99);
        request.setQuantity(5);
        request.setInventoryStatus(Product.InventoryStatus.INSTOCK);
        request.setRating(4.0);
        
        mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code", is("NEW-001")))
            .andExpect(jsonPath("$.name", is("New Product")));
    }
    
    @Test
    void createProduct_WithoutAuth_ShouldReturn403() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setCode("NEW-001");
        request.setName("New Product");
        request.setCategory("Electronics");
        request.setPrice(49.99);
        request.setQuantity(5);
        request.setInventoryStatus(Product.InventoryStatus.INSTOCK);
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }
    
    @Test
    void createProduct_AsRegularUser_ShouldReturn403() throws Exception {
        // Créer un token pour un utilisateur non-admin
        String userToken = jwtUtil.generateToken("user@example.com");
        
        ProductRequest request = new ProductRequest();
        request.setCode("NEW-001");
        request.setName("New Product");
        request.setCategory("Electronics");
        request.setPrice(49.99);
        request.setQuantity(5);
        request.setInventoryStatus(Product.InventoryStatus.INSTOCK);
        
        mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }
    
    @Test
    void createProduct_WithInvalidData_ShouldReturn400() throws Exception {
        ProductRequest request = new ProductRequest();
        // Données invalides : code manquant
        
        mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists());
    }
    
    @Test
    void createProduct_WithDuplicateCode_ShouldReturn409() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setCode("TEST-001"); // Code déjà existant
        request.setName("Duplicate Product");
        request.setCategory("Electronics");
        request.setPrice(49.99);
        request.setQuantity(5);
        request.setInventoryStatus(Product.InventoryStatus.INSTOCK);
        
        mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message", containsString("code")));
    }
    
    @Test
    void updateProduct_AsAdmin_ShouldUpdateProduct() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setCode("TEST-001-UPDATED");
        request.setName("Updated Product");
        request.setDescription("Updated Description");
        request.setCategory("Electronics");
        request.setPrice(149.99);
        request.setQuantity(20);
        request.setInventoryStatus(Product.InventoryStatus.LOWSTOCK);
        request.setRating(4.5);
        
        mockMvc.perform(patch("/api/products/{id}", testProduct.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Updated Product")))
            .andExpect(jsonPath("$.price", is(149.99)));
    }
    
    @Test
    void deleteProduct_AsAdmin_ShouldDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", testProduct.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNoContent());
        
        // Vérifier que le produit a été supprimé en utilisant la liste publique
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0))); // Plus aucun produit
    }
    
    @Test
    void getAllProducts_WithCategoryFilter_ShouldReturnFilteredProducts() throws Exception {
        // Créer un produit dans une autre catégorie
        Product otherProduct = new Product();
        otherProduct.setCode("TEST-002");
        otherProduct.setName("Other Product");
        otherProduct.setCategory("Clothing");
        otherProduct.setPrice(29.99);
        otherProduct.setQuantity(5);
        otherProduct.setInventoryStatus(Product.InventoryStatus.INSTOCK);
        productRepository.save(otherProduct);
        
        mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "10")
                .param("category", "Electronics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[*].category", everyItem(is("Electronics"))));
    }
    
    @Test
    void getAllProducts_WithSearch_ShouldReturnMatchingProducts() throws Exception {
        mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "10")
                .param("search", "Test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[*].name", everyItem(containsStringIgnoringCase("Test"))));
    }
}
