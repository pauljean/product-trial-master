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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WishlistService {
    
    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WishlistItemMapper wishlistItemMapper;
    
    public WishlistService(WishlistItemRepository wishlistItemRepository,
                          UserRepository userRepository,
                          ProductRepository productRepository,
                          WishlistItemMapper wishlistItemMapper) {
        this.wishlistItemRepository = wishlistItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.wishlistItemMapper = wishlistItemMapper;
    }
    
    @Transactional(readOnly = true)
    public List<WishlistItemResponse> getWishlistItems(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        List<WishlistItem> items = wishlistItemRepository.findByUser(user);
        return items.stream()
                .map(wishlistItemMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public WishlistItemResponse addToWishlist(String userEmail, Long productId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        Optional<WishlistItem> existingItem = wishlistItemRepository.findByUserAndProduct(user, product);
        
        WishlistItem wishlistItem;
        if (existingItem.isPresent()) {
            wishlistItem = existingItem.get();
        } else {
            wishlistItem = new WishlistItem();
            wishlistItem.setUser(user);
            wishlistItem.setProduct(product);
            wishlistItem = wishlistItemRepository.save(wishlistItem);
        }
        
        return wishlistItemMapper.toResponse(wishlistItem);
    }
    
    @Transactional
    public void removeFromWishlist(String userEmail, Long wishlistItemId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        WishlistItem wishlistItem = wishlistItemRepository.findById(wishlistItemId)
                .orElseThrow(() -> new ResourceNotFoundException("WishlistItem", "id", wishlistItemId));
        
        if (!wishlistItem.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("WishlistItem", "id", wishlistItemId);
        }
        
        wishlistItemRepository.delete(wishlistItem);
    }
}
