package com.alten.producttrial.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemRequest {
    @NotNull(message = "L'ID du produit est obligatoire")
    private Long productId;
}
