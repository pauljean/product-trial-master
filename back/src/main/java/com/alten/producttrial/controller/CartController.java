package com.alten.producttrial.controller;

import com.alten.producttrial.dto.CartItemRequest;
import com.alten.producttrial.dto.CartItemResponse;
import com.alten.producttrial.security.SecurityUtils;
import com.alten.producttrial.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "API de gestion du panier")
@SecurityRequirement(name = "bearerAuth")
public class CartController {
    
    private final CartService cartService;
    private final SecurityUtils securityUtils;
    
    public CartController(CartService cartService, SecurityUtils securityUtils) {
        this.cartService = cartService;
        this.securityUtils = securityUtils;
    }
    
    @GetMapping
    @Operation(summary = "Récupérer les articles du panier")
    public ResponseEntity<List<CartItemResponse>> getCartItems() {
        String email = securityUtils.getCurrentUserEmail();
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(cartService.getCartItems(email));
    }
    
    @PostMapping("/add")
    @Operation(summary = "Ajouter un produit au panier")
    public ResponseEntity<CartItemResponse> addToCart(
            @Valid @RequestBody CartItemRequest request) {
        String email = securityUtils.getCurrentUserEmail();
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(cartService.addToCart(email, request.getProductId(), request.getQuantity()));
    }
    
    @PatchMapping("/{cartItemId}")
    @Operation(summary = "Modifier la quantité d'un article du panier")
    public ResponseEntity<CartItemResponse> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Integer> requestBody) {
        String email = securityUtils.getCurrentUserEmail();
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Integer quantity = requestBody.get("quantity");
        return ResponseEntity.ok(cartService.updateCartItemQuantity(email, cartItemId, quantity));
    }
    
    @DeleteMapping("/{cartItemId}")
    @Operation(summary = "Supprimer un article du panier")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long cartItemId) {
        String email = securityUtils.getCurrentUserEmail();
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        cartService.removeFromCart(email, cartItemId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping
    @Operation(summary = "Vider le panier")
    public ResponseEntity<Void> clearCart() {
        String email = securityUtils.getCurrentUserEmail();
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        cartService.clearCart(email);
        return ResponseEntity.noContent().build();
    }
}
