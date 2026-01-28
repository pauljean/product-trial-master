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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {
    
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemMapper cartItemMapper;
    
    public CartService(CartItemRepository cartItemRepository,
                      UserRepository userRepository,
                      ProductRepository productRepository,
                      CartItemMapper cartItemMapper) {
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartItemMapper = cartItemMapper;
    }
    
    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartItems(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        List<CartItem> items = cartItemRepository.findByUser(user);
        return items.stream()
                .map(cartItemMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CartItemResponse addToCart(String userEmail, Long productId, Integer quantity) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        Optional<CartItem> existingItem = cartItemRepository.findByUserAndProduct(user, product);
        
        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem = cartItemRepository.save(cartItem);
        } else {
            cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem = cartItemRepository.save(cartItem);
        }
        
        return cartItemMapper.toResponse(cartItem);
    }
    
    @Transactional
    public CartItemResponse updateCartItemQuantity(String userEmail, Long cartItemId, Integer quantity) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));
        
        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("CartItem", "id", cartItemId);
        }
        
        cartItem.setQuantity(quantity);
        cartItem = cartItemRepository.save(cartItem);
        
        return cartItemMapper.toResponse(cartItem);
    }
    
    @Transactional
    public void removeFromCart(String userEmail, Long cartItemId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));
        
        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("CartItem", "id", cartItemId);
        }
        
        cartItemRepository.delete(cartItem);
    }
    
    @Transactional
    public void clearCart(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        cartItemRepository.deleteByUser(user);
    }
}
