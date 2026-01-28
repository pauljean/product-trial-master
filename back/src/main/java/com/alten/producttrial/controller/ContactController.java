package com.alten.producttrial.controller;

import com.alten.producttrial.dto.ContactRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Contact", description = "API de contact")
public class ContactController {
    
    @PostMapping("/contact")
    @Operation(summary = "Envoyer un message de contact")
    public ResponseEntity<Map<String, String>> contact(@Valid @RequestBody ContactRequest request) {
        // Dans une vraie application, on enverrait un email ici
        Map<String, String> response = new HashMap<>();
        response.put("message", "Demande de contact envoyée avec succès");
        return ResponseEntity.ok(response);
    }
}
