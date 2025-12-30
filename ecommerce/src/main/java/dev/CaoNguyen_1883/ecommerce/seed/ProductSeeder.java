package dev.CaoNguyen_1883.ecommerce.seed;

import dev.CaoNguyen_1883.ecommerce.product.entity.Brand;
import dev.CaoNguyen_1883.ecommerce.product.entity.Category;
import dev.CaoNguyen_1883.ecommerce.product.entity.Product;
import dev.CaoNguyen_1883.ecommerce.product.entity.ProductStatus;
import dev.CaoNguyen_1883.ecommerce.product.repository.BrandRepository;
import dev.CaoNguyen_1883.ecommerce.product.repository.CategoryRepository;
import dev.CaoNguyen_1883.ecommerce.product.repository.ProductRepository;
import dev.CaoNguyen_1883.ecommerce.user.entity.User;
import dev.CaoNguyen_1883.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Optional;

@Component
@Order(8)
@RequiredArgsConstructor
@Slf4j
public class ProductSeeder implements Seeder {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final UserRepository userRepository;

    @Override
    public void seed() {
        log.info("Starting product seeding from CSV...");

        if (productRepository.count() > 200) {
            log.info("Products already seeded (count > 30), skipping CSV import");
            return;
        }

        try {
            // Load CSV from resources
            InputStream inputStream = getClass().getResourceAsStream("D:/work-space/HTTM/data/products_120_new.csv");
            if (inputStream == null) {
                log.error("Could not find products_120_new.csv in resources/data/");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // Skip header line
            String headerLine = reader.readLine();
            if (headerLine == null) {
                log.error("CSV file is empty");
                return;
            }

            String line;
            int count = 0;
            int skipped = 0;

            while ((line = reader.readLine()) != null) {
                try {
                    Product product = parseProductFromCsvLine(line);
                    if (product != null) {
                        productRepository.save(product);
                        count++;
                    } else {
                        skipped++;
                    }
                } catch (Exception e) {
                    log.error("Error parsing line: {}", line, e);
                    skipped++;
                }
            }

            reader.close();
            log.info("Product seeding completed. Created: {} products, Skipped: {}", count, skipped);

        } catch (IOException e) {
            log.error("Error reading CSV file", e);
        }
    }

    private Product parseProductFromCsvLine(String line) {
        // Parse CSV line (handle quoted fields with commas)
        String[] fields = parseCsvLine(line);

        if (fields.length < 9) {
            log.warn("Invalid CSV line (not enough fields): {}", line);
            return null;
        }

        String name = fields[0];
        String slug = fields[1];
        String description = fields[2];
        String shortDescription = fields[3];
        String categorySlug = fields[4];
        String brandSlug = fields[5];
        String basePriceStr = fields[6];
        String statusStr = fields[7];
        String tags = fields[8];

        // Find category by slug
        Optional<Category> categoryOpt = categoryRepository.findBySlug(categorySlug);
        if (categoryOpt.isEmpty()) {
            log.warn("Category not found for slug: {}", categorySlug);
            return null;
        }

        // Find brand by slug
        Optional<Brand> brandOpt = brandRepository.findBySlug(brandSlug);
        if (brandOpt.isEmpty()) {
            log.warn("Brand not found for slug: {}", brandSlug);
            return null;
        }

        // Get default seller (admin or first seller)
        User seller = userRepository.findByEmail("seller@ecommerce.com")
                .or(() -> userRepository.findAll().stream()
                        .filter(u -> u.getRoles().stream()
                                .anyMatch(r -> r.getName().equals("ROLE_SELLER")))
                        .findFirst())
                .orElse(null);

        if (seller == null) {
            log.warn("No seller found for product: {}", name);
            return null;
        }

        // Get admin for approval
        User admin = userRepository.findByEmail("admin@ecommerce.com").orElse(null);

        try {
            BigDecimal basePrice = new BigDecimal(basePriceStr);
            ProductStatus status = ProductStatus.valueOf(statusStr);

            return Product.builder()
                    .name(name)
                    .slug(slug)
                    .description(description)
                    .shortDescription(shortDescription)
                    .category(categoryOpt.get())
                    .brand(brandOpt.get())
                    .seller(seller)
                    .basePrice(basePrice)
                    .status(status)
                    .tags(tags)
                    .approvedBy(admin)
                    .approvedAt(java.time.LocalDateTime.now())
                    .viewCount(0)
                    .purchaseCount(0)
                    .totalReviews(0)
                    .build();

        } catch (IllegalArgumentException e) {
            log.error("Error parsing price or status for product: {}", name, e);
            return null;
        }
    }

    /**
     * Parse CSV line handling quoted fields with commas
     */
    private String[] parseCsvLine(String line) {
        String[] result = new String[9];
        int fieldIndex = 0;
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                // End of field
                result[fieldIndex++] = currentField.toString().trim();
                currentField = new StringBuilder();
                if (fieldIndex >= 9) break; // We only need 9 fields
            } else {
                currentField.append(c);
            }
        }

        // Last field
        if (fieldIndex < 9) {
            result[fieldIndex] = currentField.toString().trim();
        }

        return result;
    }
}
