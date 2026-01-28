package com.alten.producttrial.mapper;

import com.alten.producttrial.dto.ProductRequest;
import com.alten.producttrial.dto.ProductResponse;
import com.alten.producttrial.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {
    
    private ProductMapper productMapper;
    private Product product;
    private ProductRequest productRequest;
    
    @BeforeEach
    void setUp() {
        productMapper = new ProductMapper();
        
        product = new Product();
        product.setId(1L);
        product.setCode("TEST-001");
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setImage("test.jpg");
        product.setCategory("Electronics");
        product.setPrice(99.99);
        product.setQuantity(10);
        product.setInternalReference("REF-001");
        product.setShellId(100L);
        product.setInventoryStatus(Product.InventoryStatus.INSTOCK);
        product.setRating(4.5);
        product.setCreatedAt(System.currentTimeMillis());
        product.setUpdatedAt(System.currentTimeMillis());
        
        productRequest = new ProductRequest();
        productRequest.setCode("NEW-001");
        productRequest.setName("New Product");
        productRequest.setDescription("New Description");
        productRequest.setImage("new.jpg");
        productRequest.setCategory("Clothing");
        productRequest.setPrice(49.99);
        productRequest.setQuantity(5);
        productRequest.setInternalReference("REF-002");
        productRequest.setShellId(200L);
        productRequest.setInventoryStatus(Product.InventoryStatus.LOWSTOCK);
        productRequest.setRating(3.5);
    }
    
    @Test
    void toEntity_ValidRequest_ShouldMapToEntity() {
        // When
        Product result = productMapper.toEntity(productRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("NEW-001", result.getCode());
        assertEquals("New Product", result.getName());
        assertEquals("New Description", result.getDescription());
        assertEquals("new.jpg", result.getImage());
        assertEquals("Clothing", result.getCategory());
        assertEquals(49.99, result.getPrice());
        assertEquals(5, result.getQuantity());
        assertEquals("REF-002", result.getInternalReference());
        assertEquals(200L, result.getShellId());
        assertEquals(Product.InventoryStatus.LOWSTOCK, result.getInventoryStatus());
        assertEquals(3.5, result.getRating());
    }
    
    @Test
    void toEntity_NullRequest_ShouldReturnNull() {
        // When
        Product result = productMapper.toEntity(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    void toResponse_ValidProduct_ShouldMapToResponse() {
        // When
        ProductResponse result = productMapper.toResponse(product);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TEST-001", result.getCode());
        assertEquals("Test Product", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals("test.jpg", result.getImage());
        assertEquals("Electronics", result.getCategory());
        assertEquals(99.99, result.getPrice());
        assertEquals(10, result.getQuantity());
        assertEquals("REF-001", result.getInternalReference());
        assertEquals(100L, result.getShellId());
        assertEquals(Product.InventoryStatus.INSTOCK, result.getInventoryStatus());
        assertEquals(4.5, result.getRating());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }
    
    @Test
    void toResponse_NullProduct_ShouldReturnNull() {
        // When
        ProductResponse result = productMapper.toResponse(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    void updateEntityFromRequest_ValidRequest_ShouldUpdateEntity() {
        // When
        productMapper.updateEntityFromRequest(product, productRequest);
        
        // Then
        assertEquals("NEW-001", product.getCode());
        assertEquals("New Product", product.getName());
        assertEquals("New Description", product.getDescription());
        assertEquals("new.jpg", product.getImage());
        assertEquals("Clothing", product.getCategory());
        assertEquals(49.99, product.getPrice());
        assertEquals(5, product.getQuantity());
        assertEquals("REF-002", product.getInternalReference());
        assertEquals(200L, product.getShellId());
        assertEquals(Product.InventoryStatus.LOWSTOCK, product.getInventoryStatus());
        assertEquals(3.5, product.getRating());
    }
    
    @Test
    void updateEntityFromRequest_NullRequest_ShouldNotUpdate() {
        // Given
        String originalCode = product.getCode();
        
        // When
        productMapper.updateEntityFromRequest(product, null);
        
        // Then
        assertEquals(originalCode, product.getCode());
    }
    
    @Test
    void updateEntityFromRequest_NullProduct_ShouldNotThrow() {
        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            productMapper.updateEntityFromRequest(null, productRequest);
        });
    }
}
