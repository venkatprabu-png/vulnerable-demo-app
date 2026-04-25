package com.demo;

import com.demo.model.Product;
import com.demo.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductServiceTest {

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService();
    }

    @Test
    @DisplayName("findAll returns seeded products")
    void findAllReturnsSeedData() {
        List<Product> products = productService.findAll();
        assertThat(products).isNotEmpty();
        assertThat(products.size()).isGreaterThanOrEqualTo(5);
    }

    @Test
    @DisplayName("save and findById round-trip")
    void saveAndFindById() {
        Product p = new Product(null, "Test Product", "A test", 1.99, "test");
        Product saved = productService.save(p);

        assertThat(saved.getId()).isNotNull();
        assertThat(productService.findById(saved.getId())).isEqualTo(saved);
    }

    @Test
    @DisplayName("delete removes product")
    void deleteProduct() {
        Product p = productService.save(new Product(null, "ToDelete", "desc", 5.0, "misc"));
        Long id = p.getId();

        assertThat(productService.delete(id)).isTrue();
        assertThat(productService.findById(id)).isNull();
    }

    @Test
    @DisplayName("delete returns false for missing id")
    void deleteMissing() {
        assertThat(productService.delete(9999L)).isFalse();
    }
}
