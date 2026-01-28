package com.alten.producttrial.mapper;

import com.alten.producttrial.dto.CartItemResponse;
import com.alten.producttrial.model.CartItem;
import org.springframework.stereotype.Component;

@Component
public class CartItemMapper {
    
    private final ProductMapper productMapper;
    
    public CartItemMapper(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }
    
    public CartItemResponse toResponse(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }
        
        CartItemResponse response = new CartItemResponse();
        response.setId(cartItem.getId());
        response.setQuantity(cartItem.getQuantity());
        
        // Mapper le produit en utilisant ProductMapper
        if (cartItem.getProduct() != null) {
            response.setProduct(productMapper.toResponse(cartItem.getProduct()));
        }
        
        return response;
    }
}
