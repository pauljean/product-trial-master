package com.alten.producttrial.mapper;

import com.alten.producttrial.dto.CartItemResponse;
import com.alten.producttrial.dto.ProductResponse;
import com.alten.producttrial.model.CartItem;
import com.alten.producttrial.model.Product;
import com.alten.producttrial.model.User;
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
class CartItemMapperTest {
    
    @Mock
    private ProductMapper productMapper;
    
    @InjectMocks
    private CartItemMapper cartItemMapper;
    
    private CartItem cartItem;
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
        
        cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
    }
    
    @Test
    void toResponse_ValidCartItem_ShouldMapToResponse() {
        // Given
        when(productMapper.toResponse(product)).thenReturn(productResponse);
        
        // When
        CartItemResponse result = cartItemMapper.toResponse(cartItem);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2, result.getQuantity());
        assertNotNull(result.getProduct());
        assertEquals(1L, result.getProduct().getId());
        verify(productMapper).toResponse(product);
    }
    
    @Test
    void toResponse_NullCartItem_ShouldReturnNull() {
        // When
        CartItemResponse result = cartItemMapper.toResponse(null);
        
        // Then
        assertNull(result);
        verify(productMapper, never()).toResponse(any());
    }
    
    @Test
    void toResponse_CartItemWithNullProduct_ShouldNotThrow() {
        // Given
        cartItem.setProduct(null);
        lenient().when(productMapper.toResponse(null)).thenReturn(null);
        
        // When & Then
        assertDoesNotThrow(() -> {
            CartItemResponse result = cartItemMapper.toResponse(cartItem);
            assertNotNull(result);
            assertNull(result.getProduct());
        });
    }
}
