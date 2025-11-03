package dev.CaoNguyen_1883.ecommerce.seed;

import dev.CaoNguyen_1883.ecommerce.product.entity.Category;
import dev.CaoNguyen_1883.ecommerce.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(6)
@RequiredArgsConstructor
@Slf4j
public class CategorySeeder implements Seeder {

    private final CategoryRepository categoryRepository;

    @Override
    public void seed() {
        log.info("Starting category seeding...");

        if (categoryRepository.count() > 0) {
            log.info("Categories already exist, skipping seeding");
            return;
        }

        // Root categories
        Category peripherals = createCategory("Computer Peripherals", "peripherals", "Mice, keyboards, headsets, webcams", null, 1);
        Category components = createCategory("Computer Components", "components", "CPU, GPU, RAM, Storage", null, 2);
        Category computers = createCategory("Computers", "computers", "Desktop PC, Laptops, Gaming PC", null, 3);
        Category accessories = createCategory("Accessories", "accessories", "Mouse pads, cables, monitors", null, 4);

        // Save root categories
        peripherals = categoryRepository.save(peripherals);
        components = categoryRepository.save(components);
        computers = categoryRepository.save(computers);
        accessories = categoryRepository.save(accessories);

        // Peripheral sub-categories
        List<Category> peripheralChildren = new ArrayList<>();
        peripheralChildren.add(createCategory("Gaming Mouse", "gaming-mouse", "High-performance gaming mice", peripherals, 1));
        peripheralChildren.add(createCategory("Office Mouse", "office-mouse", "Ergonomic office mice", peripherals, 2));
        peripheralChildren.add(createCategory("Mechanical Keyboard", "mechanical-keyboard", "Mechanical keyboards for gaming and work", peripherals, 3));
        peripheralChildren.add(createCategory("Membrane Keyboard", "membrane-keyboard", "Budget-friendly membrane keyboards", peripherals, 4));
        peripheralChildren.add(createCategory("Gaming Headset", "gaming-headset", "Immersive gaming headsets", peripherals, 5));
        peripheralChildren.add(createCategory("Wireless Headset", "wireless-headset", "Wireless freedom headsets", peripherals, 6));
        peripheralChildren.add(createCategory("Webcam", "webcam", "HD webcams for streaming", peripherals, 7));

        // Component sub-categories
        List<Category> componentChildren = new ArrayList<>();
        componentChildren.add(createCategory("CPU", "cpu", "Central Processing Units", components, 1));
        componentChildren.add(createCategory("GPU", "gpu", "Graphics Processing Units", components, 2));
        componentChildren.add(createCategory("RAM", "ram", "Memory modules", components, 3));
        componentChildren.add(createCategory("SSD", "ssd", "Solid State Drives", components, 4));
        componentChildren.add(createCategory("HDD", "hdd", "Hard Disk Drives", components, 5));
        componentChildren.add(createCategory("Motherboard", "motherboard", "Computer motherboards", components, 6));
        componentChildren.add(createCategory("Power Supply", "power-supply", "PSU units", components, 7));

        // Computer sub-categories
        List<Category> computerChildren = new ArrayList<>();
        computerChildren.add(createCategory("Gaming PC", "gaming-pc", "Pre-built gaming computers", computers, 1));
        computerChildren.add(createCategory("Office PC", "office-pc", "Office desktop computers", computers, 2));
        computerChildren.add(createCategory("Gaming Laptop", "gaming-laptop", "High-performance gaming laptops", computers, 3));
        computerChildren.add(createCategory("Business Laptop", "business-laptop", "Professional laptops", computers, 4));
        computerChildren.add(createCategory("Workstation", "workstation", "High-end workstations", computers, 5));

        // Accessory sub-categories
        List<Category> accessoryChildren = new ArrayList<>();
        accessoryChildren.add(createCategory("Mouse Pad", "mouse-pad", "Gaming and office mouse pads", accessories, 1));
        accessoryChildren.add(createCategory("Monitor", "monitor", "Computer monitors", accessories, 2));
        accessoryChildren.add(createCategory("Speaker", "speaker", "Desktop speakers", accessories, 3));
        accessoryChildren.add(createCategory("Cable", "cable", "Various computer cables", accessories, 4));
        accessoryChildren.add(createCategory("Desk", "desk", "Gaming and office desks", accessories, 5));
        accessoryChildren.add(createCategory("Chair", "chair", "Gaming and office chairs", accessories, 6));

        // Save all sub-categories
        categoryRepository.saveAll(peripheralChildren);
        categoryRepository.saveAll(componentChildren);
        categoryRepository.saveAll(computerChildren);
        categoryRepository.saveAll(accessoryChildren);

        int totalCategories = 4 + peripheralChildren.size() + componentChildren.size() +
                computerChildren.size() + accessoryChildren.size();
        log.info("Category seeding completed. Created: {} categories", totalCategories);
    }

    private Category createCategory(String name, String slug, String description, Category parent, int order) {
        return Category.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .parent(parent)
                .displayOrder(order)
                .build();
    }
}

