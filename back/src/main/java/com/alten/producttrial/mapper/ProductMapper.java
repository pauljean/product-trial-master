package com.alten.producttrial.mapper;

import com.alten.producttrial.dto.ProductRequest;
import com.alten.producttrial.dto.ProductResponse;
import com.alten.producttrial.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    
    public Product toEntity(ProductRequest request) {
        if (request == null) {
            return null;
        }
        
        Product product = new Product();
        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setImage(request.getImage());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setInternalReference(request.getInternalReference());
        product.setShellId(request.getShellId());
        product.setInventoryStatus(request.getInventoryStatus());
        product.setRating(request.getRating());
        
        return product;
    }
    
    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }
        
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setCode(product.getCode());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setImage(product.getImage());
        response.setCategory(product.getCategory());
        response.setPrice(product.getPrice());
        response.setQuantity(product.getQuantity());
        response.setInternalReference(product.getInternalReference());
        response.setShellId(product.getShellId());
        response.setInventoryStatus(product.getInventoryStatus());
        response.setRating(product.getRating());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        
        return response;
    }
    
    public void updateEntityFromRequest(Product product, ProductRequest request) {
        if (product == null || request == null) {
            return;
        }
        
        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setImage(request.getImage());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setInternalReference(request.getInternalReference());
        product.setShellId(request.getShellId());
        product.setInventoryStatus(request.getInventoryStatus());
        product.setRating(request.getRating());
    }
}
