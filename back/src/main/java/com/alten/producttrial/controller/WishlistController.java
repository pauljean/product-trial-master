package com.alten.producttrial.controller;

import com.alten.producttrial.dto.WishlistItemRequest;
import com.alten.producttrial.dto.WishlistItemResponse;
import com.alten.producttrial.security.SecurityUtils;
import com.alten.producttrial.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@Tag(name = "Wishlist", description = "API de gestion de la liste d'envie")
@SecurityRequirement(name = "bearerAuth")
public class WishlistController {
    
    private final WishlistService wishlistService;
    private final SecurityUtils securityUtils;
    
    public WishlistController(WishlistService wishlistService, SecurityUtils securityUtils) {
        this.wishlistService = wishlistService;
        this.securityUtils = securityUtils;
    }
    
    @GetMapping
    @Operation(summary = "Récupérer les articles de la liste d'envie")
    public ResponseEntity<List<WishlistItemResponse>> getWishlistItems() {
        String email = securityUtils.getCurrentUserEmail();
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(wishlistService.getWishlistItems(email));
    }
    
    @PostMapping("/add")
    @Operation(summary = "Ajouter un produit à la liste d'envie")
    public ResponseEntity<WishlistItemResponse> addToWishlist(
            @Valid @RequestBody WishlistItemRequest request) {
        String email = securityUtils.getCurrentUserEmail();
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(wishlistService.addToWishlist(email, request.getProductId()));
    }
    
    @DeleteMapping("/{wishlistItemId}")
    @Operation(summary = "Supprimer un article de la liste d'envie")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable Long wishlistItemId) {
        String email = securityUtils.getCurrentUserEmail();
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        wishlistService.removeFromWishlist(email, wishlistItemId);
        return ResponseEntity.noContent().build();
    }
}
