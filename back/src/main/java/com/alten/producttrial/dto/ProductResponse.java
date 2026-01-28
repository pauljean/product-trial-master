package com.alten.producttrial.dto;

import com.alten.producttrial.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String image;
    private String category;
    private Double price;
    private Integer quantity;
    private String internalReference;
    private Long shellId;
    private Product.InventoryStatus inventoryStatus;
    private Double rating;
    private Long createdAt;
    private Long updatedAt;
    
    // Méthodes utilitaires pour convertir les timestamps en LocalDateTime si nécessaire
    public LocalDateTime getCreatedAtAsLocalDateTime() {
        return createdAt != null ? 
            LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.systemDefault()) : null;
    }
    
    public LocalDateTime getUpdatedAtAsLocalDateTime() {
        return updatedAt != null ? 
            LocalDateTime.ofInstant(Instant.ofEpochMilli(updatedAt), ZoneId.systemDefault()) : null;
    }
}
