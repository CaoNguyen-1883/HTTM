# AI Recommendation API - Documentation

## Table of Contents
1. [Overview](#overview)
2. [Getting Started](#getting-started)
3. [Endpoints](#endpoints)
4. [Spring Boot Integration](#spring-boot-integration)
5. [Error Handling](#error-handling)
6. [Performance](#performance)

---

## Overview

### Technology Stack
- **Framework**: FastAPI (Python 3.9+)
- **Algorithm**: Item-Based Collaborative Filtering
- **Similarity Metric**: Cosine Similarity
- **Default Port**: 8000
- **Base URL**: `http://localhost:8000`

### Key Features
- Weighted Scoring: Products purchased more frequently have higher weights
- Fallback Strategy: Automatically switches to popular products if personalized recommendations are unavailable
- CORS Support: Pre-configured for Spring Boot (port 8080) and React (port 3000)
- Auto Documentation: Swagger UI available at `/docs`
- Health Check: `/health` endpoint for monitoring

---

## Getting Started

### Step 1: Install Dependencies
```bash
cd ml-recommendation
pip install -r requirements.txt
```

### Step 2: Train Model (if not already done)
```bash
# Using notebook or
python src/train.py
```

### Step 3: Start API Server
```bash
cd src
python api.py

# Or use uvicorn directly
uvicorn api:app --host 0.0.0.0 --port 8000 --reload
```

### Step 4: Verify Service
```bash
curl http://localhost:8000/health
```

Expected response:
```json
{
  "status": "healthy",
  "model_loaded": true,
  "message": "Service is running"
}
```

---

## Endpoints

### Endpoint Summary

| Endpoint | Method | Purpose | Use Case |
|----------|--------|---------|----------|
| `/health` | GET | Health check | Monitoring, load balancer |
| `/model/info` | GET | Model metadata | Admin dashboard |
| `/api/recommendations/similar/{product_id}` | GET | Similar products | Product detail page |
| `/api/recommendations/user` | POST | User recommendations | "Recommended for you" |
| `/api/recommendations/user/{user_id}` | GET | User recommendations (GET) | Spring Boot RestTemplate |
| `/api/recommendations/popular` | GET | Popular products | Homepage, new users |
| `/api/model/retrain` | POST | Retrain model | Admin action |
| `/` | GET | API info | Documentation |
| `/docs` | GET | Swagger UI | Interactive testing |

---

### 1. Health Check

**Endpoint:** `GET /health`

**Purpose:** Check if service is running and model is loaded.

**Request:**
```bash
curl http://localhost:8000/health
```

**Response:**
```json
{
  "status": "healthy",
  "model_loaded": true,
  "message": "Service is running"
}
```

**Response Fields:**
- `status` (string): "healthy" or "unhealthy"
- `model_loaded` (boolean): Whether model loaded successfully
- `message` (string): Detailed message

**Use Cases:**
- Kubernetes liveness/readiness probe
- Load balancer health check
- Monitoring dashboard (Grafana, Prometheus)

---

### 2. Model Info

**Endpoint:** `GET /model/info`

**Purpose:** Get information about the loaded model (training date, statistics).

**Request:**
```bash
curl http://localhost:8000/model/info
```

**Response:**
```json
{
  "status": "loaded",
  "model_path": "/path/to/item_based_cf.pkl",
  "metadata": {
    "trained_at": "2025-11-05T02:09:51.106644",
    "n_users": 102,
    "n_products": 33,
    "n_interactions": 3331,
    "sparsity": 0.0104,
    "min_co_occurrence": 5,
    "use_hybrid": true,
    "purchase_weight": 1.0,
    "view_weight": 0.3
  },
  "available_products": 33
}
```

**Use Cases:**
- Admin dashboard to track model version
- Debug when recommendations are inaccurate
- A/B testing to compare model versions

---

### 3. Similar Products

**Endpoint:** `GET /api/recommendations/similar/{product_id}`

**Purpose:** Get products similar to a specific product.

**Algorithm:** Item-Based CF with Cosine Similarity.

**Request:**
```bash
# UUID with dashes
curl "http://localhost:8000/api/recommendations/similar/ba6f86d7-6ca4-4499-aee9-108f60a9a476?limit=5"

# UUID without dashes (also works)
curl "http://localhost:8000/api/recommendations/similar/BA6F86D76CA44499AEE9108F60A9A476?limit=5"
```

**Parameters:**
- `product_id` (path, required): Product UUID (with or without dashes)
- `limit` (query, optional): Number of recommendations (1-50, default=10)

**Response:**
```json
{
  "recommendations": [
    {
      "product_id": "8878d08f-78e1-47e5-bb78-7ffaae109482",
      "score": 0.8532
    },
    {
      "product_id": "a6bc68c1-d5ec-48b8-99bb-17de744a9d84",
      "score": 0.8123
    }
  ],
  "total": 2
}
```

**Response Fields:**
- `product_id` (string): Product UUID (lowercase with dashes)
- `score` (float): Similarity score (0-1, higher means more similar)
- `total` (int): Total recommendations returned

**Use Cases:**
- Product Detail Page: "Customers also viewed"
- Cart Page: "Related products"
- Email Marketing: "You might like"

**Error Responses:**
- `404`: Product not found in model
- `503`: Model not loaded
- `500`: Internal error

---

### 4. User Personalized Recommendations (POST)

**Endpoint:** `POST /api/recommendations/user`

**Purpose:** Get personalized recommendations based on purchase history.

**Algorithm:** Weighted Item-Based CF + Fallback to Popular

**Request:**
```bash
curl -X POST "http://localhost:8000/api/recommendations/user?limit=10" \
  -H "Content-Type: application/json" \
  -d '{
    "purchased_products": [
      "ba6f86d7-6ca4-4499-aee9-108f60a9a476",
      "8878d08f-78e1-47e5-bb78-7ffaae109482",
      "ba6f86d7-6ca4-4499-aee9-108f60a9a476"
    ],
    "exclude_purchased": true
  }'
```

**Request Body:**
```json
{
  "purchased_products": ["UUID1", "UUID2", "UUID1"],
  "exclude_purchased": true
}
```

**Body Fields:**
- `purchased_products` (array[string], required): List of purchased product UUIDs
  - Can contain duplicates: Product appearing multiple times means user likes it more
  - Example: `["PROD1", "PROD1", "PROD2"]` → PROD1 has 2x weight
- `exclude_purchased` (boolean, optional): Exclude already purchased products (default: true)

**Parameters:**
- `limit` (query, optional): Number of recommendations (1-100, default=20)

**Response:**
```json
{
  "recommendations": [
    {
      "product_id": "307bb36e-6ee4-4be5-897d-e0e1fdd7b270",
      "score": 15.4321,
      "recommendation_type": "personalized"
    }
  ],
  "total": 1
}
```

**Response Fields:**
- `product_id` (string): Product UUID
- `score` (float): Recommendation score (weighted sum of similarities)
- `recommendation_type` (string): "personalized" or "popular" (fallback)
- `total` (int): Number of recommendations

**Fallback Logic:**
1. If user has purchase history → Personalized recommendations
2. If no valid products in model → Popular fallback
3. If user purchased all products → Popular (exclude purchased)

**Use Cases:**
- Homepage: "For You" section
- My Account Page: "Product suggestions"
- Email Campaign: Personalized product suggestions

---

### 5. User Personalized Recommendations (GET)

**Endpoint:** `GET /api/recommendations/user/{user_id}`

**Purpose:** Same as POST endpoint but easier to call from Spring Boot RestTemplate.

**Request:**
```bash
curl "http://localhost:8000/api/recommendations/user/550e8400-e29b-41d4-a716-446655440000?purchased_products=BA6F86D76CA44499AEE9108F60A9A476,8878D08F78E147E5BB787FFAAE109482&limit=10"
```

**Parameters:**
- `user_id` (path, required): User UUID (for logging only)
- `purchased_products` (query, required): Comma-separated UUIDs
  - Example: `PROD1,PROD2,PROD1` (duplicates OK)
- `limit` (query, optional): Number of recommendations (1-100, default=20)

**Response:** Same as POST endpoint

**Use Cases:**
- Spring Boot RestTemplate (no need for POST body)
- Simple HTTP client libraries
- Testing with browser/Postman

---

### 6. Popular Products

**Endpoint:** `GET /api/recommendations/popular`

**Purpose:** Get most popular products (most purchased).

**Algorithm:** Sum of all user interactions (purchases + views weighted)

**Request:**
```bash
curl "http://localhost:8000/api/recommendations/popular?limit=10"
```

**Parameters:**
- `limit` (query, optional): Number of recommendations (1-50, default=10)

**Response:**
```json
{
  "recommendations": [
    {
      "product_id": "a6bc68c1-d5ec-48b8-99bb-17de744a9d84",
      "score": 1303.9
    },
    {
      "product_id": "307bb36e-6ee4-4be5-897d-e0e1fdd7b270",
      "score": 1258.2
    }
  ],
  "total": 2
}
```

**Use Cases:**
- Homepage: "Best Sellers" / "Trending"
- Category Page: "Most Popular in Category"
- New Users: Cold start recommendations
- Fallback: When no personalized recommendations available

---

### 7. Retrain Model

**Endpoint:** `POST /api/model/retrain`

**Purpose:** Trigger model retraining with fresh data.

**Request:**
```bash
curl -X POST "http://localhost:8000/api/model/retrain"
```

**Response:**
```json
{
  "status": "success",
  "message": "Model retrained and reloaded"
}
```

**Process:**
1. Run `src/train.py` script
2. Load fresh data from database
3. Train new model
4. Save to `models/item_based_cf.pkl`
5. Reload model in API service (hot reload)

**Timeout:** 5 minutes (300 seconds)

**Use Cases:**
- Scheduled Job: Cron job running daily/weekly
- Admin Dashboard: Manual trigger button
- After Data Import: After importing batch of new orders

---

## Spring Boot Integration

### Setup RestTemplate Bean

```java
@Configuration
public class RecommendationConfig {
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory =
            new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(3000);  // 3 seconds
        factory.setReadTimeout(5000);     // 5 seconds

        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
    }
}
```

### Service Class

```java
@Service
@Slf4j
public class AIRecommendationService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ai.recommendation.url:http://localhost:8000}")
    private String aiServiceUrl;

    // 1. Similar Products
    public List<String> getSimilarProducts(UUID productId, int limit) {
        try {
            String url = aiServiceUrl + "/api/recommendations/similar/"
                + productId + "?limit=" + limit;

            RecommendationsResponse response = restTemplate.getForObject(
                url, RecommendationsResponse.class);

            return response.getRecommendations().stream()
                .map(rec -> rec.getProductId())
                .collect(Collectors.toList());

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Product {} not found in AI model", productId);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error calling AI recommendation service", e);
            return Collections.emptyList();
        }
    }

    // 2. User Recommendations
    public List<String> getUserRecommendations(
        UUID userId, List<UUID> purchasedProducts, int limit
    ) {
        try {
            String productsParam = purchasedProducts.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));

            String url = String.format(
                "%s/api/recommendations/user/%s?purchased_products=%s&limit=%d",
                aiServiceUrl, userId, productsParam, limit
            );

            RecommendationsResponse response = restTemplate.getForObject(
                url, RecommendationsResponse.class);

            return response.getRecommendations().stream()
                .map(rec -> rec.getProductId())
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting user recommendations", e);
            return getPopularProducts(limit);  // Fallback
        }
    }

    // 3. Popular Products (Fallback)
    public List<String> getPopularProducts(int limit) {
        try {
            String url = aiServiceUrl + "/api/recommendations/popular?limit=" + limit;
            RecommendationsResponse response = restTemplate.getForObject(
                url, RecommendationsResponse.class);

            return response.getRecommendations().stream()
                .map(rec -> rec.getProductId())
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting popular products", e);
            return Collections.emptyList();
        }
    }

    // 4. Health Check
    public boolean isServiceHealthy() {
        try {
            String url = aiServiceUrl + "/health";
            HealthResponse response = restTemplate.getForObject(
                url, HealthResponse.class);
            return "healthy".equals(response.getStatus())
                && response.isModelLoaded();
        } catch (Exception e) {
            return false;
        }
    }
}
```

### DTO Classes

```java
@Data
public class RecommendationsResponse {
    private List<RecommendationItem> recommendations;
    private int total;
}

@Data
public class RecommendationItem {
    private String productId;
    private double score;
}

@Data
public class HealthResponse {
    private String status;
    private boolean modelLoaded;
    private String message;
}
```

### Controller Example

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private AIRecommendationService recommendationService;

    @GetMapping("/{productId}/similar")
    public ResponseEntity<List<ProductDTO>> getSimilarProducts(
        @PathVariable UUID productId,
        @RequestParam(defaultValue = "10") int limit
    ) {
        List<String> similarProductIds =
            recommendationService.getSimilarProducts(productId, limit);

        // Load full product details from database
        List<ProductDTO> products =
            productService.getProductsByIds(similarProductIds);

        return ResponseEntity.ok(products);
    }

    @GetMapping("/recommended")
    public ResponseEntity<List<ProductDTO>> getRecommendedForUser(
        @AuthenticationPrincipal User user,
        @RequestParam(defaultValue = "20") int limit
    ) {
        // Get user's purchase history
        List<UUID> purchasedProducts =
            orderService.getUserPurchaseHistory(user.getId());

        // Get AI recommendations
        List<String> recommendedIds =
            recommendationService.getUserRecommendations(
                user.getId(), purchasedProducts, limit
            );

        // Load full product details
        List<ProductDTO> products =
            productService.getProductsByIds(recommendedIds);

        return ResponseEntity.ok(products);
    }
}
```

---

## Error Handling

### Common Error Codes

| Status Code | Meaning | Example |
|-------------|---------|---------|
| `400` | Bad Request | Missing required parameters |
| `404` | Not Found | Product ID not found in model |
| `503` | Service Unavailable | Model not loaded |
| `500` | Internal Server Error | Unexpected error |
| `504` | Gateway Timeout | Retrain timeout (over 5 minutes) |

### Error Response Format

```json
{
  "detail": "Product ABC123 not found in model. Available products: 33"
}
```

### Best Practices

Always check health before calling:
```java
if (!recommendationService.isServiceHealthy()) {
    return fallbackRecommendations();
}
```

Handle errors gracefully:
```java
try {
    return aiRecommendationService.getUserRecommendations(
        userId, products, 10);
} catch (Exception e) {
    log.error("AI service failed, using fallback", e);
    return popularProducts();
}
```

Set timeouts:
```java
RestTemplate restTemplate = new RestTemplate();
HttpComponentsClientHttpRequestFactory factory =
    new HttpComponentsClientHttpRequestFactory();
factory.setConnectTimeout(3000);  // 3 seconds
factory.setReadTimeout(5000);     // 5 seconds
restTemplate.setRequestFactory(factory);
```

---

## Performance

### Expected Response Times

- `/health`: under 10ms
- `/api/recommendations/similar/{id}`: 50-100ms
- `/api/recommendations/user`: 100-200ms
- `/api/recommendations/popular`: 50-100ms

### Caching Strategy (Spring Boot)

```java
@Cacheable(value = "similarProducts", key = "#productId + '-' + #limit")
public List<String> getSimilarProducts(UUID productId, int limit) {
    // Call AI service
}

@Cacheable(value = "popularProducts", key = "#limit")
public List<String> getPopularProducts(int limit) {
    // Call AI service
}

// Clear cache after retrain
@CacheEvict(value = {"similarProducts", "popularProducts"},
    allEntries = true)
public void clearRecommendationCache() {
    log.info("Recommendation cache cleared");
}
```

### Cache Configuration

```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=1h
```

---

## Algorithm Explanation

### Item-Based Collaborative Filtering

Concept: "Users who bought product A often buy product B"

Step 1: Calculate Similarity Matrix
```
Products A and B are similar if:
- Many users bought both A and B
- High cosine similarity between interaction vectors
```

Step 2: Weighted Scoring (IMPROVED)
```python
# OLD (WRONG): Simple average
score = mean(similarity(A, candidate), similarity(B, candidate))

# NEW (CORRECT): Weighted sum
score = 0
for product, purchase_count in user_purchases:
    score += purchase_count * similarity(product, candidate)
```

Example:
```
User purchased:
- Product A: 1 time
- Product B: 3 times  ← Likes more

Candidate: Product C

OLD: score = (sim(A,C) + sim(B,C)) / 2 = (0.7 + 0.8) / 2 = 0.75
NEW: score = 1 * 0.7 + 3 * 0.8 = 0.7 + 2.4 = 3.1  ← More accurate!
```

---

## Use Cases

### Product Detail Page
```
When user views "iPhone 15"
→ Call: GET /api/recommendations/similar/IPHONE15_UUID?limit=6
→ Show: "Similar products" carousel
```

### Homepage - Logged In User
```
User logged in
→ Get purchase history from database
→ Call: GET /api/recommendations/user/{userId}?purchased_products=...&limit=20
→ Show: "For You" section
```

### Homepage - Guest User
```
User not logged in
→ Call: GET /api/recommendations/popular?limit=10
→ Show: "Best Sellers" section
```

### Cart Page
```
User has 2 products in cart: A, B
→ Call: GET /api/recommendations/similar/A?limit=3
→ Call: GET /api/recommendations/similar/B?limit=3
→ Show: "You might also like"
```

---

## Notes

- **UUIDs**: API accepts both formats with dashes (`550e8400-e29b-41d4`) and without dashes (`550E8400E29B41D4`)
- **Case Insensitive**: UUIDs automatically normalized to lowercase with dashes
- **Duplicates**: List of purchased_products CAN have duplicates (product purchased multiple times)
- **Fallback**: Service automatically falls back to popular products if no personalized recommendations available
- **CORS**: Pre-configured for Spring Boot (8080) and React (3000)

---

## Quick Start Checklist

- [ ] Install dependencies: `pip install -r requirements.txt`
- [ ] Train model: Check file `models/item_based_cf.pkl` exists
- [ ] Start API: `python src/api.py`
- [ ] Check health: `curl http://localhost:8000/health`
- [ ] Test Swagger UI: Open `http://localhost:8000/docs`
- [ ] Integrate Spring Boot: Copy service code from examples above
- [ ] Setup caching: Add Spring Cache config
- [ ] Schedule retrain: Add cron job

---

For questions, check Swagger UI at `/docs` or contact the development team.
