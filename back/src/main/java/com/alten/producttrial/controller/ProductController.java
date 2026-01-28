package com.alten.producttrial.controller;

import com.alten.producttrial.dto.ProductRequest;
import com.alten.producttrial.dto.ProductResponse;
import com.alten.producttrial.security.SecurityUtils;
import com.alten.producttrial.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "API de gestion des produits")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {
    
    private final ProductService productService;
    private final SecurityUtils securityUtils;
    
    public ProductController(ProductService productService, SecurityUtils securityUtils) {
        this.productService = productService;
        this.securityUtils = securityUtils;
    }
    
    @GetMapping
    @Operation(
        summary = "Récupérer tous les produits",
        description = "Récupère la liste des produits avec pagination et filtrage optionnels. " +
                     "Sans paramètres, retourne tous les produits sans pagination."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des produits récupérée avec succès",
            content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    })
    public ResponseEntity<?> getAllProducts(
            @Parameter(description = "Numéro de page (0-indexed)", example = "0")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Taille de la page", example = "10")
            @RequestParam(required = false) Integer size,
            @Parameter(description = "Champ de tri", example = "name")
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "Direction du tri (ASC ou DESC)", example = "ASC")
            @RequestParam(required = false) String sortDir,
            @Parameter(description = "Filtrer par catégorie")
            @RequestParam(required = false) String category,
            @Parameter(description = "Recherche dans le nom, description ou code")
            @RequestParam(required = false) String search) {
        
        // Si page et size sont fournis, utiliser la pagination
        if (page != null && size != null) {
            Sort sort = Sort.by(sortBy != null ? sortBy : "name");
            if (sortDir != null && sortDir.equalsIgnoreCase("DESC")) {
                sort = sort.descending();
            } else {
                sort = sort.ascending();
            }
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ProductResponse> products = productService.getAllProducts(pageable, category, search);
            return ResponseEntity.ok(products);
        }
        
        // Sinon, retourner tous les produits sans pagination
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un produit par ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produit trouvé",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "ID du produit", required = true, example = "1")
            @PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
    
    @PostMapping
    @Operation(summary = "Créer un nouveau produit (admin uniquement)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Produit créé avec succès",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Admin uniquement"),
        @ApiResponse(responseCode = "409", description = "Code produit déjà existant")
    })
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest productRequest) {
        if (!securityUtils.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        ProductResponse product = productService.createProduct(productRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
    
    @PatchMapping("/{id}")
    @Operation(summary = "Modifier un produit (admin uniquement)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produit modifié avec succès",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Admin uniquement"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé"),
        @ApiResponse(responseCode = "409", description = "Code produit déjà existant")
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "ID du produit", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest productRequest) {
        if (!securityUtils.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        ProductResponse product = productService.updateProduct(id, productRequest);
        return ResponseEntity.ok(product);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un produit (admin uniquement)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Produit supprimé avec succès"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Admin uniquement"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID du produit", required = true, example = "1")
            @PathVariable Long id) {
        if (!securityUtils.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
