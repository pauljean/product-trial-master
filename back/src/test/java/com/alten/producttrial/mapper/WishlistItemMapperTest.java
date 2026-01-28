package com.alten.producttrial.mapper;

import com.alten.producttrial.dto.ProductResponse;
import com.alten.producttrial.dto.WishlistItemResponse;
import com.alten.producttrial.model.Product;
import com.alten.producttrial.model.WishlistItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class WishlistItemMapperTest {
    
    @Mock
    private ProductMapper productMapper;
    
    @InjectMocks
    private WishlistItemMapper wishlistItemMapper;
    
    private WishlistItem wishlistItem;
    private Product product;
    private ProductResponse productResponse;
    
    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(99.99);
        
        productResponse = new ProductResponse();
        productResponse.setId(1L);
        productResponse.setName("Test Product");
        productResponse.setPrice(99.99);
        
        wishlistItem = new WishlistItem();
        wishlistItem.setId(1L);
        wishlistItem.setProduct(product);
    }
    
    @Test
    void toResponse_ValidWishlistItem_ShouldMapToResponse() {
        // Given
        when(productMapper.toResponse(product)).thenReturn(productResponse);
        
        // When
        WishlistItemResponse result = wishlistItemMapper.toResponse(wishlistItem);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNotNull(result.getProduct());
        assertEquals(1L, result.getProduct().getId());
        verify(productMapper).toResponse(product);
    }
    
    @Test
    void toResponse_NullWishlistItem_ShouldReturnNull() {
        // When
        WishlistItemResponse result = wishlistItemMapper.toResponse(null);
        
        // Then
        assertNull(result);
        verify(productMapper, never()).toResponse(any());
    }
    
    @Test
    void toResponse_WishlistItemWithNullProduct_ShouldNotThrow() {
        // Given
        wishlistItem.setProduct(null);
        lenient().when(productMapper.toResponse(null)).thenReturn(null);
        
        // When & Then
        assertDoesNotThrow(() -> {
            WishlistItemResponse result = wishlistItemMapper.toResponse(wishlistItem);
            assertNotNull(result);
            assertNull(result.getProduct());
        });
    }
}
