package dev.CaoNguyen_1883.ecommerce.seed;

import dev.CaoNguyen_1883.ecommerce.product.entity.Product;
import dev.CaoNguyen_1883.ecommerce.product.repository.ProductRepository;
import dev.CaoNguyen_1883.ecommerce.tracking.entity.UserProductView;
import dev.CaoNguyen_1883.ecommerce.tracking.repository.UserProductViewRepository;
import dev.CaoNguyen_1883.ecommerce.user.entity.User;
import dev.CaoNguyen_1883.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Seeds realistic user product view data for recommendation system
 * Implements smart behavior patterns:
 * - Normal users: 15-30 views, focused on 2-3 categories
 * - Active users: 30-60 views, diverse interests
 * - Power users: 60-100+ views, research-oriented
 */
@Component
@Order(13)
@RequiredArgsConstructor
@Slf4j
public class UserViewSeeder implements Seeder {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final UserProductViewRepository viewRepository;

    private static final int TARGET_VIEW_COUNT = 10000; // Target total views
    private static final int DAYS_BACK = 60; // View data from last 60 days

    // User behavior categories
    private static final double NORMAL_USER_RATIO = 0.60;  // 60%
    private static final double ACTIVE_USER_RATIO = 0.30;  // 30%
    private static final double POWER_USER_RATIO = 0.10;   // 10%

    @Override
    public void seed() {
        log.info("Starting user product view seeding...");

        // Check if already seeded
        long existingViews = viewRepository.count();
        if (existingViews > 5000) {
            log.info("Views already seeded (count: {}), skipping", existingViews);
            return;
        }

        // Get all customer users and products
        List<User> customers = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream()
                        .anyMatch(r -> r.getName().equals("ROLE_CUSTOMER")))
                .collect(Collectors.toList());

        List<Product> products = productRepository.findAll();

        if (customers.isEmpty() || products.isEmpty()) {
            log.warn("No customers or products found. Skipping view seeding.");
            return;
        }

        log.info("Generating views for {} customers across {} products",
                 customers.size(), products.size());

        // Group products by category for realistic browsing
        Map<UUID, List<Product>> productsByCategory = products.stream()
                .collect(Collectors.groupingBy(p -> p.getCategory().getId()));

        Random random = new Random(42); // Fixed seed for reproducibility
        List<UserProductView> allViews = new ArrayList<>();

        // Categorize users
        int normalUserCount = (int) (customers.size() * NORMAL_USER_RATIO);
        int activeUserCount = (int) (customers.size() * ACTIVE_USER_RATIO);

        for (int i = 0; i < customers.size(); i++) {
            User customer = customers.get(i);
            UserBehaviorType behaviorType;

            if (i < normalUserCount) {
                behaviorType = UserBehaviorType.NORMAL;
            } else if (i < normalUserCount + activeUserCount) {
                behaviorType = UserBehaviorType.ACTIVE;
            } else {
                behaviorType = UserBehaviorType.POWER;
            }

            List<UserProductView> userViews = generateUserViews(
                    customer, products, productsByCategory, behaviorType, random);
            allViews.addAll(userViews);
        }

        // Batch save views
        log.info("Saving {} product views...", allViews.size());
        int batchSize = 500;
        for (int i = 0; i < allViews.size(); i += batchSize) {
            int end = Math.min(i + batchSize, allViews.size());
            viewRepository.saveAll(allViews.subList(i, end));

            if ((i / batchSize) % 5 == 0) {
                log.debug("Saved {}/{} views", end, allViews.size());
            }
        }

        log.info("User view seeding completed. Total views: {}", allViews.size());
    }

    /**
     * Generate views for a single user based on their behavior type
     */
    private List<UserProductView> generateUserViews(
            User user,
            List<Product> allProducts,
            Map<UUID, List<Product>> productsByCategory,
            UserBehaviorType behaviorType,
            Random random) {

        List<UserProductView> views = new ArrayList<>();
        int viewCount = behaviorType.getRandomViewCount(random);

        // Select favorite categories for this user
        List<UUID> categoryIds = new ArrayList<>(productsByCategory.keySet());
        int favoriteCategories = behaviorType.getFavoriteCategoryCount(random);
        Collections.shuffle(categoryIds, random);
        List<UUID> userFavoriteCategories = categoryIds.subList(
                0, Math.min(favoriteCategories, categoryIds.size()));

        // Generate views with realistic timestamp distribution
        LocalDateTime now = LocalDateTime.now();
        Set<UUID> viewedProductIds = new HashSet<>();

        for (int i = 0; i < viewCount; i++) {
            Product product;

            // 70% from favorite categories, 30% random exploration
            if (random.nextDouble() < 0.7 && !userFavoriteCategories.isEmpty()) {
                UUID randomCategory = userFavoriteCategories.get(
                        random.nextInt(userFavoriteCategories.size()));
                List<Product> categoryProducts = productsByCategory.get(randomCategory);

                if (categoryProducts != null && !categoryProducts.isEmpty()) {
                    product = categoryProducts.get(random.nextInt(categoryProducts.size()));
                } else {
                    product = allProducts.get(random.nextInt(allProducts.size()));
                }
            } else {
                product = allProducts.get(random.nextInt(allProducts.size()));
            }

            // Generate realistic timestamp (last 60 days, peak hours weighted)
            LocalDateTime viewTime = generateRealisticTimestamp(now, random);

            // Determine view count (some products viewed multiple times)
            int productViewCount = 1;
            if (viewedProductIds.contains(product.getId())) {
                // Revisit - increase view count
                productViewCount = random.nextInt(3) + 1; // 1-3 additional views
            } else {
                viewedProductIds.add(product.getId());
            }

            UserProductView view = new UserProductView(user.getId(), product.getId());
            view.setViewCount(productViewCount);
            view.setLastViewedAt(viewTime);
            views.add(view);
        }

        return views;
    }

    /**
     * Generate realistic timestamp with peak hours weighting
     * Peak hours: 9-11h, 13-15h, 19-22h
     */
    private LocalDateTime generateRealisticTimestamp(LocalDateTime now, Random random) {
        // Random day in last DAYS_BACK days
        int daysAgo = random.nextInt(DAYS_BACK);
        LocalDateTime baseDate = now.minusDays(daysAgo);

        // Generate hour with peak hour bias
        int hour;
        double hourRoll = random.nextDouble();

        if (hourRoll < 0.3) {
            // Morning peak: 9-11h
            hour = 9 + random.nextInt(3);
        } else if (hourRoll < 0.5) {
            // Afternoon peak: 13-15h
            hour = 13 + random.nextInt(3);
        } else if (hourRoll < 0.75) {
            // Evening peak: 19-22h
            hour = 19 + random.nextInt(4);
        } else {
            // Off-peak: any hour
            hour = random.nextInt(24);
        }

        int minute = random.nextInt(60);
        int second = random.nextInt(60);

        return baseDate.withHour(hour).withMinute(minute).withSecond(second);
    }

    /**
     * User behavior types with different viewing patterns
     */
    private enum UserBehaviorType {
        NORMAL(15, 30, 2, 3),    // 15-30 views, 2-3 favorite categories
        ACTIVE(30, 60, 3, 5),    // 30-60 views, 3-5 favorite categories
        POWER(60, 120, 4, 7);    // 60-120 views, 4-7 favorite categories

        private final int minViews;
        private final int maxViews;
        private final int minCategories;
        private final int maxCategories;

        UserBehaviorType(int minViews, int maxViews, int minCategories, int maxCategories) {
            this.minViews = minViews;
            this.maxViews = maxViews;
            this.minCategories = minCategories;
            this.maxCategories = maxCategories;
        }

        public int getRandomViewCount(Random random) {
            return minViews + random.nextInt(maxViews - minViews + 1);
        }

        public int getFavoriteCategoryCount(Random random) {
            return minCategories + random.nextInt(maxCategories - minCategories + 1);
        }
    }
}
