package com.fiosmj.app.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiosmj.app.model.Product;
import com.fiosmj.app.repository.ProductRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public ProductController(ProductRepository productRepository, ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/products")
    public List<Map<String, Object>> getAllProducts() {
        return productRepository.findByActiveTrueOrderByDisplayOrderAscIdAsc()
                .stream().map(this::toMap).toList();
    }

    @GetMapping("/products/{id}")
    public Map<String, Object> getProductById(@PathVariable Long id) {
        Product p = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + id));
        return toMap(p);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "app", "Fios MJ API");
    }

    public Map<String, Object> toMap(Product p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("name", p.getName());
        m.put("description", p.getDescription());
        m.put("price", p.getPrice());
        m.put("imageUrl", p.getImageUrl());
        m.put("category", p.getCategory());
        m.put("stock", p.getStock());
        m.put("displayOrder", p.getDisplayOrder());
        m.put("active", p.isActive());
        m.put("createdAt", p.getCreatedAt());
        m.put("updatedAt", p.getUpdatedAt());

        // Parse sizesJson → List<Map>
        List<Map<String, Object>> sizes = null;
        if (p.getSizesJson() != null && !p.getSizesJson().isBlank()) {
            try {
                sizes = objectMapper.readValue(p.getSizesJson(),
                        new TypeReference<List<Map<String, Object>>>() {});
            } catch (Exception ignored) {}
        }
        m.put("sizes", sizes);
        return m;
    }
}
