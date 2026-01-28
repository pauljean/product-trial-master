package com.alten.producttrial.mapper;

import com.alten.producttrial.dto.WishlistItemResponse;
import com.alten.producttrial.model.WishlistItem;
import org.springframework.stereotype.Component;

@Component
public class WishlistItemMapper {
    
    private final ProductMapper productMapper;
    
    public WishlistItemMapper(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }
    
    public WishlistItemResponse toResponse(WishlistItem wishlistItem) {
        if (wishlistItem == null) {
            return null;
        }
        
        WishlistItemResponse response = new WishlistItemResponse();
        response.setId(wishlistItem.getId());
        
        // Mapper le produit en utilisant ProductMapper
        if (wishlistItem.getProduct() != null) {
            response.setProduct(productMapper.toResponse(wishlistItem.getProduct()));
        }
        
        return response;
    }
}
