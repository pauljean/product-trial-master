package com.alten.producttrial.service;

import com.alten.producttrial.dto.ProductRequest;
import com.alten.producttrial.dto.ProductResponse;
import com.alten.producttrial.exception.DuplicateResourceException;
import com.alten.producttrial.exception.ResourceNotFoundException;
import com.alten.producttrial.mapper.ProductMapper;
import com.alten.producttrial.model.Product;
import com.alten.producttrial.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private ProductMapper productMapper;
    
    @InjectMocks
    private ProductService productService;
    
    private Product product;
    private ProductRequest productRequest;
    private ProductResponse productResponse;
    
    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setCode("TEST-001");
        product.setName("Test Product");
        product.setCategory("Electronics");
        product.setPrice(99.99);
        product.setQuantity(10);
        product.setInventoryStatus(Product.InventoryStatus.INSTOCK);
        
        productRequest = new ProductRequest();
        productRequest.setCode("TEST-001");
        productRequest.setName("Test Product");
        productRequest.setCategory("Electronics");
        productRequest.setPrice(99.99);
        productRequest.setQuantity(10);
        productRequest.setInventoryStatus(Product.InventoryStatus.INSTOCK);
        
        productResponse = new ProductResponse();
        productResponse.setId(1L);
        productResponse.setCode("TEST-001");
        productResponse.setName("Test Product");
        productResponse.setCategory("Electronics");
        productResponse.setPrice(99.99);
        productResponse.setQuantity(10);
        productResponse.setInventoryStatus(Product.InventoryStatus.INSTOCK);
    }
    
    @Test
    void getAllProducts_WithPagination_ShouldReturnPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);
        
        // When
        Page<ProductResponse> result = productService.getAllProducts(pageable, null, null);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
        verify(productMapper, times(1)).toResponse(any(Product.class));
    }
    
    @Test
    void getAllProducts_WithoutPagination_ShouldReturnList() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findAll()).thenReturn(products);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);
        
        // When
        List<ProductResponse> result = productService.getAllProducts();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository).findAll();
        verify(productMapper, times(1)).toResponse(any(Product.class));
    }
    
    @Test
    void getProductById_ExistingId_ShouldReturnProduct() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(productResponse);
        
        // When
        ProductResponse result = productService.getProductById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals("TEST-001", result.getCode());
        verify(productRepository).findById(1L);
        verify(productMapper).toResponse(product);
    }
    
    @Test
    void getProductById_NonExistingId_ShouldThrowException() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(999L);
        });
        verify(productRepository).findById(999L);
        verify(productMapper, never()).toResponse(any());
    }
    
    @Test
    void createProduct_ValidRequest_ShouldCreateProduct() {
        // Given
        when(productRepository.findByCode("TEST-001")).thenReturn(Optional.empty());
        when(productMapper.toEntity(productRequest)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);
        
        // When
        ProductResponse result = productService.createProduct(productRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("TEST-001", result.getCode());
        verify(productRepository).findByCode("TEST-001");
        verify(productRepository).save(product);
        verify(productMapper).toEntity(productRequest);
        verify(productMapper).toResponse(product);
    }
    
    @Test
    void createProduct_DuplicateCode_ShouldThrowException() {
        // Given
        when(productRepository.findByCode("TEST-001")).thenReturn(Optional.of(product));
        
        // When & Then
        assertThrows(DuplicateResourceException.class, () -> {
            productService.createProduct(productRequest);
        });
        verify(productRepository).findByCode("TEST-001");
        verify(productRepository, never()).save(any());
    }
    
    @Test
    void updateProduct_ExistingId_ShouldUpdateProduct() {
        // Given
        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setCode("TEST-001-UPDATED");
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findByCode("TEST-001-UPDATED")).thenReturn(Optional.empty());
        when(productRepository.save(product)).thenReturn(updatedProduct);
        when(productMapper.toResponse(updatedProduct)).thenReturn(productResponse);
        
        productRequest.setCode("TEST-001-UPDATED");
        
        // When
        ProductResponse result = productService.updateProduct(1L, productRequest);
        
        // Then
        assertNotNull(result);
        verify(productRepository).findById(1L);
        verify(productRepository).save(product);
        verify(productMapper).updateEntityFromRequest(product, productRequest);
    }
    
    @Test
    void updateProduct_NonExistingId_ShouldThrowException() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(999L, productRequest);
        });
        verify(productRepository).findById(999L);
        verify(productRepository, never()).save(any());
    }
    
    @Test
    void deleteProduct_ExistingId_ShouldDeleteProduct() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);
        
        // When
        productService.deleteProduct(1L);
        
        // Then
        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }
    
    @Test
    void deleteProduct_NonExistingId_ShouldThrowException() {
        // Given
        when(productRepository.existsById(999L)).thenReturn(false);
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.deleteProduct(999L);
        });
        verify(productRepository).existsById(999L);
        verify(productRepository, never()).deleteById(any());
    }
    
    @Test
    void getAllProducts_WithCategoryFilter_ShouldFilterByCategory() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);
        
        // When
        Page<ProductResponse> result = productService.getAllProducts(pageable, "Electronics", null);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }
    
    @Test
    void getAllProducts_WithSearchFilter_ShouldFilterBySearch() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);
        
        // When
        Page<ProductResponse> result = productService.getAllProducts(pageable, null, "Test");
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }
    
    @Test
    void getAllProducts_WithCategoryAndSearch_ShouldFilterByBoth() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);
        
        // When
        Page<ProductResponse> result = productService.getAllProducts(pageable, "Electronics", "Test");
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }
    
    @Test
    void updateProduct_WithDuplicateCode_ShouldThrowException() {
        // Given
        ProductRequest requestWithDuplicateCode = new ProductRequest();
        requestWithDuplicateCode.setCode("DUPLICATE-001");
        requestWithDuplicateCode.setName("New Product");
        requestWithDuplicateCode.setCategory("Electronics");
        requestWithDuplicateCode.setPrice(99.99);
        requestWithDuplicateCode.setQuantity(10);
        requestWithDuplicateCode.setInventoryStatus(Product.InventoryStatus.INSTOCK);
        
        Product existingProductWithCode = new Product();
        existingProductWithCode.setId(2L);
        existingProductWithCode.setCode("DUPLICATE-001");
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findByCode("DUPLICATE-001")).thenReturn(Optional.of(existingProductWithCode));
        
        // When & Then
        assertThrows(DuplicateResourceException.class, () -> {
            productService.updateProduct(1L, requestWithDuplicateCode);
        });
        verify(productRepository).findById(1L);
        verify(productRepository).findByCode("DUPLICATE-001");
        verify(productRepository, never()).save(any());
    }
    
    @Test
    void updateProduct_SameCode_ShouldNotThrowException() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);
        
        productRequest.setCode("TEST-001"); // Même code
        
        // When
        ProductResponse result = productService.updateProduct(1L, productRequest);
        
        // Then
        assertNotNull(result);
        verify(productRepository).findById(1L);
        verify(productRepository, never()).findByCode(anyString()); // Ne devrait pas vérifier si le code est le même
        verify(productRepository).save(product);
    }
}
