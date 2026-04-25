package com.demo.service;

import com.demo.model.Product;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ProductService — handles product CRUD and uses Apache Commons IO
 * for file-based export/import operations.
 *
 * NOTE: Uses commons-io 2.6 which is vulnerable to CVE-2021-29425
 * (relative path traversal via FilenameUtils.normalize).
 * Fix: upgrade commons-io to 2.7+
 */
@Service
public class ProductService {

    private static final Logger logger = LogManager.getLogger(ProductService.class);

    private final Map<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(1);

    public ProductService() {
        // Seed data
        save(new Product(null, "Widget Alpha", "Standard widget", 9.99, "widgets"));
        save(new Product(null, "Widget Beta",  "Premium widget",  19.99, "widgets"));
        save(new Product(null, "Gadget X",     "Entry gadget",    49.99, "gadgets"));
        save(new Product(null, "Gadget Pro",   "Pro-grade gadget",99.99, "gadgets"));
        save(new Product(null, "Tool Basic",   "Basic toolset",   14.99, "tools"));
    }

    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    public Product findById(Long id) {
        return store.get(id);
    }

    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(idSeq.getAndIncrement());
        }
        store.put(product.getId(), product);
        logger.info("Saved product id={}", product.getId());
        return product;
    }

    public boolean delete(Long id) {
        return store.remove(id) != null;
    }

    /**
     * Exports product catalogue to a file under the given base directory.
     *
     * VULNERABLE: passes user-supplied fileName through FilenameUtils.normalize()
     * from commons-io 2.6 — susceptible to CVE-2021-29425 path traversal.
     * Fix: upgrade commons-io to 2.7+ where normalize() rejects null-byte paths.
     */
    public String exportCatalogue(String baseDir, String fileName) throws IOException {
        // CVE-2021-29425 — normalize does not reject relative traversal in 2.6
        String safeName = FilenameUtils.normalize(baseDir + File.separator + fileName);
        File outFile = new File(safeName);

        StringBuilder sb = new StringBuilder("id,name,category,price\n");
        store.values().forEach(p ->
            sb.append(p.getId()).append(",")
              .append(p.getName()).append(",")
              .append(p.getCategory()).append(",")
              .append(p.getPrice()).append("\n")
        );

        FileUtils.writeStringToFile(outFile, sb.toString(), StandardCharsets.UTF_8);
        logger.info("Exported catalogue to {}", safeName);
        return safeName;
    }
}
