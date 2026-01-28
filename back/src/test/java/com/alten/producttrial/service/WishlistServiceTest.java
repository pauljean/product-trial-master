package com.alten.producttrial.service;

import com.alten.producttrial.dto.WishlistItemResponse;
import com.alten.producttrial.exception.ResourceNotFoundException;
import com.alten.producttrial.exception.UnauthorizedAccessException;
import com.alten.producttrial.mapper.WishlistItemMapper;
import com.alten.producttrial.model.Product;
import com.alten.producttrial.model.User;
import com.alten.producttrial.model.WishlistItem;
import com.alten.producttrial.repository.ProductRepository;
import com.alten.producttrial.repository.UserRepository;
import com.alten.producttrial.repository.WishlistItemRepository;
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
class WishlistServiceTest {
    
    @Mock
    private WishlistItemRepository wishlistItemRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private WishlistItemMapper wishlistItemMapper;
    
    @InjectMocks
    private WishlistService wishlistService;
    
    private User user;
    private Product product;
    private WishlistItem wishlistItem;
    private WishlistItemResponse wishlistItemResponse;
    
    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        
        wishlistItem = new WishlistItem();
        wishlistItem.setId(1L);
        wishlistItem.setUser(user);
        wishlistItem.setProduct(product);
        
        wishlistItemResponse = new WishlistItemResponse();
        wishlistItemResponse.setId(1L);
    }
    
    @Test
    void getWishlistItems_ValidUser_ShouldReturnWishlistItems() {
        // Given
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(wishlistItemRepository.findByUser(user)).thenReturn(Arrays.asList(wishlistItem));
        when(wishlistItemMapper.toResponse(wishlistItem)).thenReturn(wishlistItemResponse);
        
        // When
        List<WishlistItemResponse> result = wishlistService.getWishlistItems("user@example.com");
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findByEmail("user@example.com");
        verify(wishlistItemRepository).findByUser(user);
        verify(wishlistItemMapper).toResponse(wishlistItem);
    }
    
    @Test
    void getWishlistItems_NonExistingUser_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            wishlistService.getWishlistItems("user@example.com");
        });
        verify(userRepository).findByEmail("user@example.com");
        verify(wishlistItemRepository, never()).findByUser(any());
    }
    
    @Test
    void addToWishlist_NewItem_ShouldCreateWishlistItem() {
        // Given
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(wishlistItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());
        when(wishlistItemRepository.save(any(WishlistItem.class))).thenReturn(wishlistItem);
        when(wishlistItemMapper.toResponse(wishlistItem)).thenReturn(wishlistItemResponse);
        
        // When
        WishlistItemResponse result = wishlistService.addToWishlist("user@example.com", 1L);
        
        // Then
        assertNotNull(result);
        verify(userRepository).findByEmail("user@example.com");
        verify(productRepository).findById(1L);
        verify(wishlistItemRepository).findByUserAndProduct(user, product);
        verify(wishlistItemRepository).save(any(WishlistItem.class));
    }
    
    @Test
    void addToWishlist_ExistingItem_ShouldReturnExistingItem() {
        // Given
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(wishlistItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.of(wishlistItem));
        when(wishlistItemMapper.toResponse(wishlistItem)).thenReturn(wishlistItemResponse);
        
        // When
        WishlistItemResponse result = wishlistService.addToWishlist("user@example.com", 1L);
        
        // Then
        assertNotNull(result);
        verify(wishlistItemRepository, never()).save(any());
    }
    
    @Test
    void addToWishlist_NonExistingUser_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            wishlistService.addToWishlist("user@example.com", 1L);
        });
        verify(userRepository).findByEmail("user@example.com");
        verify(productRepository, never()).findById(any());
    }
    
    @Test
    void addToWishlist_NonExistingProduct_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            wishlistService.addToWishlist("user@example.com", 1L);
        });
        verify(userRepository).findByEmail("user@example.com");
        verify(productRepository).findById(1L);
        verify(wishlistItemRepository, never()).save(any());
    }
    
    @Test
    void removeFromWishlist_ValidRequest_ShouldDeleteWishlistItem() {
        // Given
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(wishlistItemRepository.findById(1L)).thenReturn(Optional.of(wishlistItem));
        doNothing().when(wishlistItemRepository).delete(wishlistItem);
        
        // When
        wishlistService.removeFromWishlist("user@example.com", 1L);
        
        // Then
        verify(wishlistItemRepository).delete(wishlistItem);
    }
    
    @Test
    void removeFromWishlist_DifferentUser_ShouldThrowException() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        wishlistItem.setUser(otherUser);
        
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(wishlistItemRepository.findById(1L)).thenReturn(Optional.of(wishlistItem));
        
        // When & Then
        assertThrows(UnauthorizedAccessException.class, () -> {
            wishlistService.removeFromWishlist("user@example.com", 1L);
        });
        verify(wishlistItemRepository, never()).delete(any());
    }
    
    @Test
    void removeFromWishlist_NonExistingItem_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(wishlistItemRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            wishlistService.removeFromWishlist("user@example.com", 1L);
        });
        verify(wishlistItemRepository, never()).delete(any());
    }
}
