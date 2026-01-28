package com.alten.producttrial.dto;

import com.alten.producttrial.model.Product;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    
    @NotBlank(message = "Le code produit est obligatoire")
    @Size(max = 100, message = "Le code produit ne peut pas dépasser 100 caractères")
    private String code;
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 255, message = "Le nom ne peut pas dépasser 255 caractères")
    private String name;
    
    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String description;
    
    private String image;
    
    @NotBlank(message = "La catégorie est obligatoire")
    @Size(max = 100, message = "La catégorie ne peut pas dépasser 100 caractères")
    private String category;
    
    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être supérieur à 0")
    @Digits(integer = 10, fraction = 2, message = "Le prix doit avoir au maximum 10 chiffres avant la virgule et 2 après")
    private Double price;
    
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 0, message = "La quantité ne peut pas être négative")
    private Integer quantity;
    
    @Size(max = 100, message = "La référence interne ne peut pas dépasser 100 caractères")
    private String internalReference;
    
    private Long shellId;
    
    @NotNull(message = "Le statut d'inventaire est obligatoire")
    private Product.InventoryStatus inventoryStatus;
    
    @DecimalMin(value = "0.0", message = "La note ne peut pas être négative")
    @DecimalMax(value = "5.0", message = "La note ne peut pas dépasser 5.0")
    private Double rating;
}
