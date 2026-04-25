package com.demo.controller;

import com.demo.model.Product;
import com.demo.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Products.
 *
 * Logging goes through Log4j 2.14.1 — vulnerable to CVE-2021-44228 (Log4Shell).
 * Jackson 2.13.2 handles JSON — vulnerable to CVE-2022-42003 / CVE-2022-42004.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LogManager.getLogger(ProductController.class);

    private final ProductService productService;
    private final ObjectMapper objectMapper;

    public ProductController(ProductService productService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public List<Product> getAll() {
        logger.info("GET /api/products");
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        Product p = productService.findById(id);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(p);
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        // Log4j 2.14.1: if name contains ${jndi:...}, CVE-2021-44228 triggers
        logger.info("Creating product: {}", product.getName());
        return ResponseEntity.ok(productService.save(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return ResponseEntity.ok(productService.save(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> delete(@PathVariable Long id) {
        boolean deleted = productService.delete(id);
        return ResponseEntity.ok(Map.of("deleted", deleted));
    }

    @GetMapping("/search")
    public List<Product> search(@RequestParam String query) {
        logger.info("Product search query: {}", query);
        String lq = query.toLowerCase();
        return productService.findAll().stream()
                .filter(p -> p.getName().toLowerCase().contains(lq)
                          || p.getCategory().toLowerCase().contains(lq))
                .collect(java.util.stream.Collectors.toList());
    }
}
