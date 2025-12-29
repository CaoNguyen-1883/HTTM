package dev.CaoNguyen_1883.ecommerce.seed;

import dev.CaoNguyen_1883.ecommerce.product.entity.*;
import dev.CaoNguyen_1883.ecommerce.product.repository.BrandRepository;
import dev.CaoNguyen_1883.ecommerce.product.repository.CategoryRepository;
import dev.CaoNguyen_1883.ecommerce.product.repository.ProductRepository;
import dev.CaoNguyen_1883.ecommerce.user.entity.User;
import dev.CaoNguyen_1883.ecommerce.user.repository.UserRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(9)
@RequiredArgsConstructor
@Slf4j
public class ProductWithVariantsSeeder implements Seeder {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final UserRepository userRepository;

    @Override
    public void seed() {
        log.info("Starting product+variants seeding from CSV...");

        // Check if we should skip
        if (productRepository.count() > 150) {
            log.info(
                "Products already seeded (count > 150), skipping CSV import"
            );
            return;
        }

        try {
            // Load CSV from resources
            InputStream inputStream = getClass().getResourceAsStream(
                "/data/products_with_variants.csv"
            );
            if (inputStream == null) {
                log.warn(
                    "Could not find products_with_variants.csv in resources/data/"
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

            // Group variants by product slug
            Map<String, List<ProductVariantRow>> productVariantsMap =
                new LinkedHashMap<>();
            Map<String, ProductRow> productRowMap = new HashMap<>();

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    // Parse CSV line
                    String[] fields = parseCsvLine(line);
                    if (fields.length < 16) {
                        log.warn(
                            "Line {}: Invalid CSV (not enough fields)",
                            lineNumber
                        );
                        continue;
                    }

                    String slug = fields[1].trim();

                    // Store product info (first occurrence)
                    if (!productRowMap.containsKey(slug)) {
                        ProductRow productRow = new ProductRow();
                        productRow.name = fields[0].trim();
                        productRow.slug = slug;
                        productRow.description = fields[2].trim();
                        productRow.shortDescription = fields[3].trim();
                        productRow.categorySlug = fields[4].trim();
                        productRow.brandSlug = fields[5].trim();
                        productRow.basePrice = fields[6].trim();
                        productRow.status = fields[7].trim();
                        productRow.tags = fields[8].trim();
                        productRowMap.put(slug, productRow);
                    }

                    // Store variant info
                    ProductVariantRow variantRow = new ProductVariantRow();
                    variantRow.name = fields[9].trim();
                    variantRow.sku = fields[10].trim();
                    variantRow.price = fields[11].trim();
                    variantRow.stock = fields[12].trim();
                    variantRow.attributes = fields[13].trim();
                    variantRow.specifications = fields[14].trim();
                    variantRow.isDefault = fields[15].trim();

                    productVariantsMap
                        .computeIfAbsent(slug, k -> new ArrayList<>())
                        .add(variantRow);
                } catch (Exception e) {
                    log.error("Error parsing line {}: {}", lineNumber, line, e);
                }
            }

            reader.close();

            // Now create products with variants
            int productsCreated = 0;
            int variantsCreated = 0;
            int skipped = 0;

            User seller = getDefaultSeller();
            User admin = getAdmin();

            for (Map.Entry<
                String,
                ProductRow
            > entry : productRowMap.entrySet()) {
                String slug = entry.getKey();
                ProductRow productRow = entry.getValue();
                List<ProductVariantRow> variantRows = productVariantsMap.get(
                    slug
                );

                if (variantRows == null || variantRows.isEmpty()) {
                    log.warn("No variants found for product: {}", slug);
                    skipped++;
                    continue;
                }

                // Check if product already exists (skip duplicates)
                Optional<Product> existingProduct =
                    productRepository.findBySlug(slug);
                if (existingProduct.isPresent()) {
                    log.debug("Product already exists, skipping: {}", slug);
                    skipped++;
                    continue;
                }

                try {
                    // Create product
                    Product product = createProduct(productRow, seller, admin);
                    if (product == null) {
                        skipped++;
                        continue;
                    }

                    // Create variants
                    for (ProductVariantRow variantRow : variantRows) {
                        ProductVariant variant = createVariant(
                            variantRow,
                            product
                        );
                        if (variant != null) {
                            product.addVariant(variant);
                            variantsCreated++;
                        }
                    }

                    // Save product with variants
                    productRepository.save(product);
                    productsCreated++;
                } catch (Exception e) {
                    log.error("Error creating product: {}", slug, e);
                    skipped++;
                }
            }

            log.info(
                "Product+Variants seeding completed. Products: {}, Variants: {}, Skipped: {}",
                productsCreated,
                variantsCreated,
                skipped
            );
        } catch (IOException e) {
            log.error("Error reading CSV file", e);
        }
    }

    private Product createProduct(ProductRow row, User seller, User admin) {
        // Find category
        Optional<Category> categoryOpt = categoryRepository.findBySlug(
            row.categorySlug
        );
        if (categoryOpt.isEmpty()) {
            log.warn("Category not found: {}", row.categorySlug);
            return null;
        }

        // Find brand
        Optional<Brand> brandOpt = brandRepository.findBySlug(row.brandSlug);
        if (brandOpt.isEmpty()) {
            log.warn("Brand not found: {}", row.brandSlug);
            return null;
        }

        if (seller == null) {
            log.warn("No seller found for product: {}", row.name);
            return null;
        }

        try {
            BigDecimal basePrice = new BigDecimal(row.basePrice);
            ProductStatus status = ProductStatus.valueOf(row.status);

            return Product.builder()
                .name(row.name)
                .slug(row.slug)
                .description(row.description)
                .shortDescription(row.shortDescription)
                .category(categoryOpt.get())
                .brand(brandOpt.get())
                .seller(seller)
                .basePrice(basePrice)
                .status(status)
                .tags(row.tags)
                .approvedBy(admin)
                .approvedAt(java.time.LocalDateTime.now())
                .viewCount(0)
                .purchaseCount(0)
                .totalReviews(0)
                .build();
        } catch (Exception e) {
            log.error("Error parsing product data: {}", row.name, e);
            return null;
        }
    }

    private ProductVariant createVariant(
        ProductVariantRow row,
        Product product
    ) {
        try {
            BigDecimal price = new BigDecimal(row.price);
            int stock = Integer.parseInt(row.stock);
            boolean isDefault = Boolean.parseBoolean(row.isDefault);

            return ProductVariant.builder()
                .product(product)
                .sku(row.sku)
                .name(row.name)
                .price(price)
                .stock(stock)
                .reservedStock(0)
                .attributes(row.attributes)
                .specifications(row.specifications)
                .isDefault(isDefault)
                .displayOrder(isDefault ? 0 : 1)
                .build();
        } catch (Exception e) {
            log.error(
                "Error parsing variant: {} (SKU: {})",
                row.name,
                row.sku,
                e
            );
            return null;
        }
    }

    private User getDefaultSeller() {
        return userRepository
            .findByEmail("seller@ecommerce.com")
            .or(() ->
                userRepository
                    .findAll()
                    .stream()
                    .filter(u ->
                        u
                            .getRoles()
                            .stream()
                            .anyMatch(r -> r.getName().equals("ROLE_SELLER"))
                    )
                    .findFirst()
            )
            .orElse(null);
    }

    private User getAdmin() {
        return userRepository.findByEmail("admin@ecommerce.com").orElse(null);
    }

    /**
     * Parse CSV line handling quoted fields with commas and escaped quotes
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // Check for escaped quote (two consecutive quotes)
                if (
                    inQuotes &&
                    i + 1 < line.length() &&
                    line.charAt(i + 1) == '"'
                ) {
                    // This is an escaped quote, add single quote to field
                    currentField.append('"');
                    i++; // Skip next quote
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // Field separator (outside quotes)
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        // Add last field
        fields.add(currentField.toString());

        return fields.toArray(new String[0]);
    }

    // Inner classes for temporary storage
    private static class ProductRow {

        String name, slug, description, shortDescription;
        String categorySlug, brandSlug, basePrice, status, tags;
    }

    private static class ProductVariantRow {

        String name, sku, price, stock;
        String attributes, specifications, isDefault;
    }
}
