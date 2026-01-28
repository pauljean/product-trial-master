package com.alten.producttrial.service;

import com.alten.producttrial.dto.ProductRequest;
import com.alten.producttrial.dto.ProductResponse;
import com.alten.producttrial.exception.DuplicateResourceException;
import com.alten.producttrial.exception.ResourceNotFoundException;
import com.alten.producttrial.mapper.ProductMapper;
import com.alten.producttrial.model.Product;
import com.alten.producttrial.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        log.info("ProductService initialisé");
    }
    
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable, String category, String search) {
        log.debug("Récupération des produits avec pagination - page: {}, size: {}, category: {}, search: {}", 
            pageable.getPageNumber(), pageable.getPageSize(), category, search);
        
        Specification<Product> spec = Specification.where(null);
        
        if (category != null && !category.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
        }
        
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = "%" + search.toLowerCase() + "%";
            Specification<Product> searchSpec = (root, query, cb) -> 
                cb.or(
                    cb.like(cb.lower(root.get("name")), searchLower),
                    cb.like(cb.lower(root.get("description")), searchLower),
                    cb.like(cb.lower(root.get("code")), searchLower)
                );
            spec = spec.and(searchSpec);
        }
        
        Page<Product> products = productRepository.findAll(spec, pageable);
        log.info("{} produits trouvés", products.getTotalElements());
        
        return products.map(productMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        log.debug("Récupération de tous les produits sans pagination");
        List<Product> products = productRepository.findAll();
        log.info("{} produits récupérés", products.size());
        return products.stream()
            .map(productMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.debug("Récupération du produit avec l'ID: {}", id);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Produit non trouvé avec l'ID: {}", id);
                return new ResourceNotFoundException("Product", "id", id);
            });
        log.debug("Produit trouvé: {}", product.getCode());
        return productMapper.toResponse(product);
    }
    
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Création d'un nouveau produit avec le code: {}", request.getCode());
        
        // Vérifier l'unicité du code
        if (productRepository.findByCode(request.getCode()).isPresent()) {
            log.error("Tentative de création d'un produit avec un code existant: {}", request.getCode());
            throw new DuplicateResourceException("Product", "code", request.getCode());
        }
        
        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);
        log.info("Produit créé avec succès - ID: {}, Code: {}", savedProduct.getId(), savedProduct.getCode());
        
        return productMapper.toResponse(savedProduct);
    }
    
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("Mise à jour du produit avec l'ID: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Tentative de mise à jour d'un produit inexistant - ID: {}", id);
                return new ResourceNotFoundException("Product", "id", id);
            });
        
        // Vérifier l'unicité du code si le code a changé
        if (!product.getCode().equals(request.getCode()) && 
            productRepository.findByCode(request.getCode()).isPresent()) {
            log.error("Tentative de mise à jour avec un code existant: {}", request.getCode());
            throw new DuplicateResourceException("Product", "code", request.getCode());
        }
        
        productMapper.updateEntityFromRequest(product, request);
        Product updatedProduct = productRepository.save(product);
        log.info("Produit mis à jour avec succès - ID: {}, Code: {}", updatedProduct.getId(), updatedProduct.getCode());
        
        return productMapper.toResponse(updatedProduct);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Suppression du produit avec l'ID: {}", id);
        
        if (!productRepository.existsById(id)) {
            log.warn("Tentative de suppression d'un produit inexistant - ID: {}", id);
            throw new ResourceNotFoundException("Product", "id", id);
        }
        
        productRepository.deleteById(id);
        log.info("Produit supprimé avec succès - ID: {}", id);
    }
}
