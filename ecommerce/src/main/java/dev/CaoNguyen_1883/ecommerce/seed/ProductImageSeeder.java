package dev.CaoNguyen_1883.ecommerce.seed;

import dev.CaoNguyen_1883.ecommerce.product.entity.Product;
import dev.CaoNguyen_1883.ecommerce.product.entity.ProductImage;
import dev.CaoNguyen_1883.ecommerce.product.entity.ProductVariant;
import dev.CaoNguyen_1883.ecommerce.product.repository.ProductImageRepository;
import dev.CaoNguyen_1883.ecommerce.product.repository.ProductRepository;
import dev.CaoNguyen_1883.ecommerce.product.repository.ProductVariantRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
@RequiredArgsConstructor
@Slf4j
public class ProductImageSeeder implements Seeder {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;

    @Override
    public void seed() {
        log.info("Starting product images seeding from CSV...");

        // Check if we should skip - high threshold to allow re-running for new products
        long imageCount = imageRepository.count();
        if (imageCount > 500) {
            log.info(
                "Product images already seeded (count > 500: {}), skipping",
                imageCount
            );
            return;
        }

        log.info(
            "Current image count: {}. Proceeding with seeding...",
            imageCount
        );

        try {
            // Load CSV from resources
            InputStream inputStream = getClass().getResourceAsStream(
                "/data/product_images.csv"
            );
            if (inputStream == null) {
                log.warn(
                    "Could not find product_images.csv in resources/data/"
                );
                return;
            }

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream)
            );

            // Skip header line
            String headerLine = reader.readLine();
            if (headerLine == null) {
                log.error("CSV file is empty");
                return;
            }

            String line;
            int lineNumber = 1;
            int imagesCreated = 0;
            int skipped = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    // Parse CSV line
                    String[] fields = parseCsvLine(line);
                    if (fields.length < 6) {
                        log.warn(
                            "Line {}: Invalid CSV (expected 6 fields, got {})",
                            lineNumber,
                            fields.length
                        );
                        skipped++;
                        continue;
                    }

                    String productSlug = fields[0].trim();
                    String variantName = fields[1].trim();
                    String imageUrl = fields[2].trim();
                    boolean isPrimary = Boolean.parseBoolean(fields[3].trim());
                    int displayOrder = Integer.parseInt(fields[4].trim());
                    String altText = fields[5].trim();

                    // Find product
                    Optional<Product> productOpt = productRepository.findBySlug(
                        productSlug
                    );
                    if (productOpt.isEmpty()) {
                        log.debug(
                            "Line {}: Product not found: {}",
                            lineNumber,
                            productSlug
                        );
                        skipped++;
                        continue;
                    }

                    Product product = productOpt.get();
                    ProductVariant variant = null;

                    // Find variant if specified
                    if (!variantName.isEmpty()) {
                        variant = variantRepository
                            .findByProductIdAndName(
                                product.getId(),
                                variantName
                            )
                            .orElse(null);

                        if (variant == null) {
                            log.debug(
                                "Line {}: Variant '{}' not found for product '{}'",
                                lineNumber,
                                variantName,
                                productSlug
                            );
                            skipped++;
                            continue;
                        }
                    }

                    // Check if image already exists (avoid duplicates)
                    boolean exists;
                    if (variant != null) {
                        exists =
                            imageRepository.existsByProductAndVariantAndImageUrl(
                                product,
                                variant,
                                imageUrl
                            );
                    } else {
                        exists =
                            imageRepository.existsByProductAndVariantIsNullAndImageUrl(
                                product,
                                imageUrl
                            );
                    }

                    if (exists) {
                        log.debug(
                            "Line {}: Image already exists, skipping",
                            lineNumber
                        );
                        skipped++;
                        continue;
                    }

                    // Create image
                    ProductImage image = ProductImage.builder()
                        .product(product)
                        .variant(variant)
                        .imageUrl(imageUrl)
                        .altText(altText)
                        .isPrimary(isPrimary)
                        .displayOrder(displayOrder)
                        .build();

                    imageRepository.save(image);
                    imagesCreated++;

                    if (imagesCreated % 100 == 0) {
                        log.info("Created {} images...", imagesCreated);
                    }
                } catch (Exception e) {
                    log.error("Error parsing line {}: {}", lineNumber, line, e);
                    skipped++;
                }
            }

            reader.close();

            log.info(
                "Product images seeding completed. Created: {}, Skipped: {}",
                imagesCreated,
                skipped
            );
        } catch (IOException e) {
            log.error("Error reading CSV file", e);
        }
    }

    /**
     * Parse CSV line handling quoted fields with commas
     */
    private String[] parseCsvLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // Check for escaped quote
                if (
                    inQuotes &&
                    i + 1 < line.length() &&
                    line.charAt(i + 1) == '"'
                ) {
                    currentField.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }
}
