package com.alten.producttrial.service;

import com.alten.producttrial.dto.CartItemResponse;
import com.alten.producttrial.exception.ResourceNotFoundException;
import com.alten.producttrial.exception.UnauthorizedAccessException;
import com.alten.producttrial.mapper.CartItemMapper;
import com.alten.producttrial.model.CartItem;
import com.alten.producttrial.model.Product;
import com.alten.producttrial.model.User;
import com.alten.producttrial.repository.CartItemRepository;
import com.alten.producttrial.repository.ProductRepository;
import com.alten.producttrial.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    
    @Mock
    private CartItemRepository cartItemRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private CartItemMapper cartItemMapper;
    
    @InjectMocks
    private CartService cartService;
    
    private User user;
    private Product product;
    private CartItem cartItem;
    private CartItemResponse cartItemResponse;
    
    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(99.99);
        
        cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        
        cartItemResponse = new CartItemResponse();
        cartItemResponse.setId(1L);
        cartItemResponse.setQuantity(2);
    }
    
    @Test
    void getCartItems_ValidUser_ShouldReturnCartItems() {
        // Given
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUser(user)).thenReturn(Arrays.asList(cartItem));
        when(cartItemMapper.toResponse(cartItem)).thenReturn(cartItemResponse);
        
        // When
        List<CartItemResponse> result = cartService.getCartItems("user@example.com");
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findByEmail("user@example.com");
        verify(cartItemRepository).findByUser(user);
        verify(cartItemMapper).toResponse(cartItem);
    }
    
    @Test
    void getCartItems_NonExistingUser_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.getCartItems("user@example.com");
        });
        verify(userRepository).findByEmail("user@example.com");
        verify(cartItemRepository, never()).findByUser(any());
    }
    
    @Test
    void addToCart_NewItem_ShouldCreateCartItem() {
        // Given
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartItemMapper.toResponse(cartItem)).thenReturn(cartItemResponse);
        
        // When
        CartItemResponse result = cartService.addToCart("user@example.com", 1L, 2);
        
        // Then
        assertNotNull(result);
        verify(userRepository).findByEmail("user@example.com");
        verify(productRepository).findById(1L);
        verify(cartItemRepository).findByUserAndProduct(user, product);
        verify(cartItemRepository).save(any(CartItem.class));
    }
    
    @Test
    void addToCart_ExistingItem_ShouldUpdateQuantity() {
        // Given
        CartItem existingItem = new CartItem();
        existingItem.setId(1L);
        existingItem.setQuantity(1);
        existingItem.setUser(user);
        existingItem.setProduct(product);
        
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.of(existingItem));
        when(cartItemRepository.save(existingItem)).thenReturn(existingItem);
        when(cartItemMapper.toResponse(existingItem)).thenReturn(cartItemResponse);
        
        // When
        CartItemResponse result = cartService.addToCart("user@example.com", 1L, 2);
        
        // Then
        assertNotNull(result);
        assertEquals(3, existingItem.getQuantity()); // 1 + 2
        verify(cartItemRepository).save(existingItem);
    }
    
    @Test
    void addToCart_NonExistingUser_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.addToCart("user@example.com", 1L, 2);
        });
        verify(userRepository).findByEmail("user@example.com");
        verify(productRepository, never()).findById(any());
    }
    
    @Test
    void addToCart_NonExistingProduct_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.addToCart("user@example.com", 1L, 2);
        });
        verify(userRepository).findByEmail("user@example.com");
        verify(productRepository).findById(1L);
        verify(cartItemRepository, never()).save(any());
    }
    
    @Test
    void updateCartItemQuantity_ValidRequest_ShouldUpdateQuantity() {
        // Given
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
        when(cartItemMapper.toResponse(cartItem)).thenReturn(cartItemResponse);
        
        // When
        CartItemResponse result = cartService.updateCartItemQuantity("user@example.com", 1L, 5);
        
        // Then
        assertNotNull(result);
        assertEquals(5, cartItem.getQuantity());
        verify(cartItemRepository).save(cartItem);
    }
    
    @Test
    void updateCartItemQuantity_DifferentUser_ShouldThrowException() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        cartItem.setUser(otherUser);
        
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
        
        // When & Then
        assertThrows(UnauthorizedAccessException.class, () -> {
            cartService.updateCartItemQuantity("user@example.com", 1L, 5);
        });
        verify(cartItemRepository, never()).save(any());
    }
    
    @Test
    void removeFromCart_ValidRequest_ShouldDeleteCartItem() {
        // Given
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
        doNothing().when(cartItemRepository).delete(cartItem);
        
        // When
        cartService.removeFromCart("user@example.com", 1L);
        
        // Then
        verify(cartItemRepository).delete(cartItem);
    }
    
    @Test
    void removeFromCart_DifferentUser_ShouldThrowException() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        cartItem.setUser(otherUser);
        
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
        
        // When & Then
        assertThrows(UnauthorizedAccessException.class, () -> {
            cartService.removeFromCart("user@example.com", 1L);
        });
        verify(cartItemRepository, never()).delete(any());
    }
    
    @Test
    void clearCart_ValidUser_ShouldDeleteAllItems() {
        // Given
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        doNothing().when(cartItemRepository).deleteByUser(user);
        
        // When
        cartService.clearCart("user@example.com");
        
        // Then
        verify(cartItemRepository).deleteByUser(user);
    }
}
