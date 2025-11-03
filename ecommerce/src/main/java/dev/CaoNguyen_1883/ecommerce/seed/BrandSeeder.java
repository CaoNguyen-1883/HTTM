package dev.CaoNguyen_1883.ecommerce.seed;

import dev.CaoNguyen_1883.ecommerce.product.entity.Brand;
import dev.CaoNguyen_1883.ecommerce.product.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(7)
@RequiredArgsConstructor
@Slf4j
public class BrandSeeder implements Seeder {

    private final BrandRepository brandRepository;

    @Override
    public void seed() {
        log.info("Starting brand seeding...");

        if (brandRepository.count() > 0) {
            log.info("Brands already exist, skipping seeding");
            return;
        }

        List<Brand> brands = new ArrayList<>();

        // Gaming Peripherals Brands
        brands.add(createBrand("Logitech", "logitech", "Premium peripherals", "Switzerland", true));
        brands.add(createBrand("Razer", "razer", "Gaming lifestyle brand", "USA", true));
        brands.add(createBrand("Corsair", "corsair", "High-performance gaming gear", "USA", true));
        brands.add(createBrand("SteelSeries", "steelseries", "Esports gaming gear", "Denmark", true));
        brands.add(createBrand("HyperX", "hyperx", "Gaming peripherals", "USA", false));
        brands.add(createBrand("Logitech G", "logitech-g", "Gaming division of Logitech", "Switzerland", false));

        // Keyboard Brands
        brands.add(createBrand("Keychron", "keychron", "Mechanical keyboards", "Hong Kong", false));
        brands.add(createBrand("Ducky", "ducky", "Premium mechanical keyboards", "Taiwan", false));
        brands.add(createBrand("Anne Pro", "anne-pro", "Compact mechanical keyboards", "China", false));

        // Component Brands
        brands.add(createBrand("Intel", "intel", "Processor manufacturer", "USA", true));
        brands.add(createBrand("AMD", "amd", "CPU and GPU manufacturer", "USA", true));
        brands.add(createBrand("NVIDIA", "nvidia", "Graphics card manufacturer", "USA", true));
        brands.add(createBrand("ASUS", "asus", "Computer hardware", "Taiwan", true));
        brands.add(createBrand("MSI", "msi", "Gaming hardware", "Taiwan", false));
        brands.add(createBrand("Gigabyte", "gigabyte", "Motherboards and graphics cards", "Taiwan", false));
        brands.add(createBrand("ASRock", "asrock", "Motherboards", "Taiwan", false));

        // Memory & Storage
        brands.add(createBrand("Kingston", "kingston", "Memory and storage", "Taiwan", false));
        brands.add(createBrand("Crucial", "crucial", "Memory and SSD", "USA", false));
        brands.add(createBrand("Samsung", "samsung", "Consumer electronics", "South Korea", true));
        brands.add(createBrand("Western Digital", "western-digital", "Storage solutions", "USA", false));
        brands.add(createBrand("Seagate", "seagate", "Hard drives", "USA", false));

        // Computer Brands
        brands.add(createBrand("Dell", "dell", "Computer manufacturer", "USA", false));
        brands.add(createBrand("HP", "hp", "Computer and printer", "USA", false));
        brands.add(createBrand("Lenovo", "lenovo", "PC manufacturer", "China", false));
        brands.add(createBrand("Acer", "acer", "Computer hardware", "Taiwan", false));

        // Monitor Brands
        brands.add(createBrand("LG", "lg", "Electronics manufacturer", "South Korea", false));
        brands.add(createBrand("BenQ", "benq", "Display solutions", "Taiwan", false));
        brands.add(createBrand("ViewSonic", "viewsonic", "Visual solutions", "USA", false));

        brandRepository.saveAll(brands);
        log.info("Brand seeding completed. Created: {} brands", brands.size());
    }

    private Brand createBrand(String name, String slug, String description, String country, boolean featured) {
        return Brand.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .countryOfOrigin(country)
                .isFeatured(featured)
                .build();
    }
}
