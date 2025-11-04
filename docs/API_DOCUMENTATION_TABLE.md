# Bảng tổng hợp API Endpoints

## 1. Authentication APIs

| Method | Endpoint | Description | Request Body | Response | Auth Required | Roles |
|--------|----------|-------------|--------------|----------|---------------|-------|
| POST | /api/auth/login | Đăng nhập | LoginRequest (email, password, expectedRole) | AuthResponse (accessToken, refreshToken, user) | No | Public |
| POST | /api/auth/register | Đăng ký tài khoản | RegisterRequest (email, password, fullName, phone) | AuthResponse | No | Public |
| POST | /api/auth/refresh-token | Làm mới access token | RefreshTokenRequest (refreshToken) | AuthResponse | No | Public |
| GET | /api/auth/me | Lấy thông tin user hiện tại | - | UserDTO | Yes | All |

## 2. User Management APIs

| Method | Endpoint | Description | Request Body | Response | Auth Required | Roles |
|--------|----------|-------------|--------------|----------|---------------|-------|
| GET | /api/users | Lấy danh sách users (paginated) | Params: page, size, sort | Page<UserDTO> | Yes | ADMIN |
| GET | /api/users/search | Tìm kiếm users | Params: keyword, page, size | Page<UserDTO> | Yes | ADMIN |
| GET | /api/users/role/{roleName} | Lấy users theo role | Path: roleName | Page<UserDTO> | Yes | ADMIN |
| GET | /api/users/{id} | Lấy user by ID | Path: id | UserDTO | Yes | ADMIN, STAFF |
| POST | /api/users | Tạo user mới | UserRequest | UserDTO | Yes | ADMIN |
| PUT | /api/users/{id} | Cập nhật user | UserUpdateRequest | UserDTO | Yes | ADMIN |
| POST | /api/users/{id}/roles | Gán roles cho user | Body: List<String> roleNames | UserDTO | Yes | ADMIN |
| DELETE | /api/users/{id}/roles | Xóa roles của user | Body: List<String> roleNames | UserDTO | Yes | ADMIN |
| POST | /api/users/{id}/change-password | Đổi mật khẩu | PasswordChangeRequest | String message | Yes | ADMIN, Owner |
| POST | /api/users/{id}/verify-email | Verify email | - | String message | Yes | ADMIN |
| DELETE | /api/users/{id} | Soft delete user | - | String message | Yes | ADMIN |
| PATCH | /api/users/{id}/restore | Restore user đã xóa | - | UserDTO | Yes | ADMIN |

## 3. Role Management APIs

| Method | Endpoint | Description | Request Body | Response | Auth Required | Roles |
|--------|----------|-------------|--------------|----------|---------------|-------|
| GET | /api/roles | Lấy tất cả roles | - | List<RoleDTO> | Yes | ADMIN |
| GET | /api/roles/{id} | Lấy role by ID | Path: id | RoleDTO | Yes | ADMIN |
| POST | /api/roles | Tạo role mới | RoleRequest (name, description) | RoleDTO | Yes | ADMIN |
| PUT | /api/roles/{id} | Cập nhật role | RoleRequest | RoleDTO | Yes | ADMIN |
| POST | /api/roles/{id}/permissions | Gán permissions cho role | Body: List<UUID> permissionIds | RoleDTO | Yes | ADMIN |
| DELETE | /api/roles/{id}/permissions | Xóa permissions của role | Body: List<UUID> permissionIds | RoleDTO | Yes | ADMIN |
| DELETE | /api/roles/{id} | Xóa role | - | String message | Yes | ADMIN |

## 4. Permission Management APIs

| Method | Endpoint | Description | Request Body | Response | Auth Required | Roles |
|--------|----------|-------------|--------------|----------|---------------|-------|
| GET | /api/permissions | Lấy tất cả permissions | - | List<PermissionDTO> | Yes | ADMIN |
| GET | /api/permissions/{id} | Lấy permission by ID | Path: id | PermissionDTO | Yes | ADMIN |
| POST | /api/permissions | Tạo permission mới | PermissionRequest | PermissionDTO | Yes | ADMIN |
| PUT | /api/permissions/{id} | Cập nhật permission | PermissionRequest | PermissionDTO | Yes | ADMIN |
| DELETE | /api/permissions/{id} | Xóa permission | - | String message | Yes | ADMIN |

## 5. Product APIs

### 5.1 Public Product APIs

| Method | Endpoint | Description | Query Params | Response | Auth Required | Roles |
|--------|----------|-------------|--------------|----------|---------------|-------|
| GET | /api/products | Lấy tất cả products | page, size, sort, keyword, categoryId, brandId, minPrice, maxPrice | Page<ProductSummaryDto> | No | Public |
| GET | /api/products/{id} | Chi tiết product by ID | Path: id | ProductDto | No | Public |
| GET | /api/products/slug/{slug} | Chi tiết product by slug | Path: slug | ProductDto | No | Public |
| GET | /api/products/search | Tìm kiếm products | keyword, page, size | Page<ProductSummaryDto> | No | Public |
| GET | /api/products/category/{categoryId} | Products theo category | Path: categoryId, Params: page, size | Page<ProductSummaryDto> | No | Public |
| GET | /api/products/brand/{brandId} | Products theo brand | Path: brandId, Params: page, size | Page<ProductSummaryDto> | No | Public |
| GET | /api/products/trending | Sản phẩm trending | limit (default: 10) | List<ProductSummaryDto> | No | Public |
| GET | /api/products/best-sellers | Sản phẩm bán chạy | limit (default: 10) | List<ProductSummaryDto> | No | Public |
| GET | /api/products/top-rated | Sản phẩm rating cao | limit (default: 10) | List<ProductSummaryDto> | No | Public |
| GET | /api/products/{id}/similar | Sản phẩm tương tự | Path: id, Params: limit | List<ProductSummaryDto> | No | Public |

### 5.2 Seller Product APIs

| Method | Endpoint | Description | Request Body | Response | Auth Required | Roles |
|--------|----------|-------------|--------------|----------|---------------|-------|
| POST | /api/products | Tạo product mới | ProductRequest | ProductDto | Yes | SELLER |
| GET | /api/products/my-products | Lấy products của seller | Params: page, size | Page<ProductDto> | Yes | SELLER |
| PUT | /api/products/{id} | Cập nhật product | ProductUpdateRequest | ProductDto | Yes | SELLER (owner) |
| DELETE | /api/products/{id} | Xóa product | - | String message | Yes | SELLER (owner) |

### 5.3 Admin/Staff Product APIs

| Method | Endpoint | Description | Request Body | Response | Auth Required | Roles |
|--------|----------|-------------|--------------|----------|---------------|-------|
| GET | /api/products/pending | Products chờ duyệt | Params: page, size | Page<ProductDto> | Yes | STAFF, ADMIN |
| GET | /api/products/admin/all | Tất cả products (admin) | Params: page, size | Page<ProductDto> | Yes | STAFF, ADMIN |
| GET | /api/products/status/{status} | Products theo status | Path: status, Params: page, size | Page<ProductDto> | Yes | STAFF, ADMIN |
| POST | /api/products/{id}/approve | Duyệt product | Body: ProductApprovalRequest | ProductDto | Yes | STAFF, ADMIN |
| POST | /api/products/{id}/reject | Từ chối product | Body: ProductApprovalRequest (rejectionReason) | ProductDto | Yes | STAFF, ADMIN |

## 6. Category APIs

| Method | Endpoint | Description | Query Params | Response | Auth Required | Roles |
|--------|----------|-------------|--------------|----------|---------------|-------|
| GET | /api/categories | Lấy tất cả categories | - | List<CategoryDto> | No | Public |
| GET | /api/categories/root | Root categories with children | - | List<CategoryDto> | No | Public |
| GET | /api/categories/{id} | Category by ID | Path: id | CategoryDto | No | Public |
| GET | /api/categories/slug/{slug} | Category by slug | Path: slug | CategoryDto | No | Public |
| GET | /api/categories/{id}/children | Sub-categories | Path: id | List<CategoryDto> | No | Public |
| POST | /api/categories | Tạo category | CategoryRequest | CategoryDto | Yes | ADMIN |
| PUT | /api/categories/{id} | Cập nhật category | CategoryRequest | CategoryDto | Yes | ADMIN |
| DELETE | /api/categories/{id} | Xóa category | - | String message | Yes | ADMIN |
| PATCH | /api/categories/{id}/restore | Restore category | - | CategoryDto | Yes | ADMIN |

## 7. Brand APIs

| Method | Endpoint | Description | Request Body | Response | Auth Required | Roles |
|--------|----------|-------------|--------------|----------|---------------|-------|
| GET | /api/brands | Lấy tất cả brands | - | List<BrandDto> | No | Public |
| GET | /api/brands/{id} | Brand by ID | Path: id | BrandDto | No | Public |
| GET | /api/brands/slug/{slug} | Brand by slug | Path: slug | BrandDto | No | Public |
| POST | /api/brands | Tạo brand | BrandRequest | BrandDto | Yes | ADMIN |
| PUT | /api/brands/{id} | Cập nhật brand | BrandRequest | BrandDto | Yes | ADMIN |
| DELETE | /api/brands/{id} | Xóa brand | - | String message | Yes | ADMIN |
| PATCH | /api/brands/{id}/restore | Restore brand | - | BrandDto | Yes | ADMIN |

## 8. Cart APIs

| Method | Endpoint | Description | Request Body | Response | Auth Required | Roles |
|--------|----------|-------------|--------------|----------|---------------|-------|
| GET | /api/cart | Lấy giỏ hàng hiện tại | - | CartDto | Yes | CUSTOMER |
| POST | /api/cart/items | Thêm item vào giỏ | AddToCartRequest (variantId, quantity) | CartDto | Yes | CUSTOMER |
| PUT | /api/cart/items/{itemId} | Cập nhật quantity | UpdateCartItemRequest (quantity) | CartDto | Yes | CUSTOMER |
| DELETE | /api/cart/items/{itemId} | Xóa item khỏi giỏ | - | CartDto | Yes | CUSTOMER |
| DELETE | /api/cart | Xóa toàn bộ giỏ hàng | - | String message | Yes | CUSTOMER |
| POST | /api/cart/sync | Sync cart data | Body: List<CartItemDto> | CartDto | Yes | CUSTOMER |
| GET | /api/cart/count | Đếm số item trong giỏ | - | Integer | Yes | CUSTOMER |

## 9. Order APIs

### 9.1 Customer Order APIs

| Method | Endpoint | Description | Request Body | Response | Auth Required | Roles |
|--------|----------|-------------|--------------|----------|---------------|-------|
| POST | /api/orders | Tạo order từ cart | CreateOrderRequest | OrderDto | Yes | CUSTOMER |
| GET | /api/orders/my-orders | Lấy orders của mình | Params: page, size | Page<OrderDto> | Yes | CUSTOMER |
| GET | /api/orders/{orderId} | Chi tiết order | Path: orderId | OrderDto | Yes | CUSTOMER |
| GET | /api/orders/number/{orderNumber} | Order by order number | Path: orderNumber | OrderDto | Yes | CUSTOMER |
| POST | /api/orders/{orderId}/cancel | Hủy order | CancelOrderRequest (cancelReason) | OrderDto | Yes | CUSTOMER |

### 9.2 Admin/Staff Order APIs

| Method | Endpoint | Description | Request Body | Response | Auth Required | Roles |
|--------|----------|-------------|--------------|----------|---------------|-------|
| GET | /api/orders | Tất cả orders | Params: page, size, sort | Page<OrderDto> | Yes | STAFF, ADMIN |
| GET | /api/orders/status/{status} | Orders theo status | Path: status, Params: page, size | Page<OrderDto> | Yes | STAFF, ADMIN |
| GET | /api/orders/payment-status/{status} | Orders theo payment status | Path: status, Params: page, size | Page<OrderDto> | Yes | STAFF, ADMIN |
| GET | /api/orders/pending | Pending orders | Params: page, size | Page<OrderDto> | Yes | STAFF, ADMIN |
| GET | /api/orders/search | Tìm kiếm orders | Params: keyword, page, size | Page<OrderDto> | Yes | STAFF, ADMIN |
| GET | /api/orders/date-range | Orders trong khoảng thời gian | Params: startDate, endDate, page, size | Page<OrderDto> | Yes | STAFF, ADMIN |
| POST | /api/orders/{orderId}/confirm | Xác nhận order | - | OrderDto | Yes | STAFF, ADMIN |
| POST | /api/orders/{orderId}/processing | Đánh dấu đang xử lý | - | OrderDto | Yes | STAFF, ADMIN |
| POST | /api/orders/{orderId}/ship | Đánh dấu đã ship | - | OrderDto | Yes | STAFF, ADMIN |
| POST | /api/orders/{orderId}/deliver | Đánh dấu đã giao | - | OrderDto | Yes | STAFF, ADMIN |
| PUT | /api/orders/{orderId}/status | Cập nhật status | UpdateOrderStatusRequest | OrderDto | Yes | STAFF, ADMIN |
| POST | /api/orders/{orderId}/payment/paid | Đánh dấu đã thanh toán | - | OrderDto | Yes | STAFF, ADMIN |
| POST | /api/orders/{orderId}/payment/failed | Đánh dấu thanh toán thất bại | - | OrderDto | Yes | STAFF, ADMIN |

### 9.3 Order Statistics APIs

| Method | Endpoint | Description | Query Params | Response | Auth Required | Roles |
|--------|----------|-------------|--------------|----------|---------------|-------|
| GET | /api/orders/statistics | Thống kê orders | - | OrderStatistics | Yes | ADMIN, STAFF |
| GET | /api/orders/revenue/total | Tổng doanh thu | - | BigDecimal | Yes | ADMIN |
| GET | /api/orders/revenue/date-range | Doanh thu theo thời gian | startDate, endDate | BigDecimal | Yes | ADMIN |

## 10. Review APIs

### 10.1 Public Review APIs

| Method | Endpoint | Description | Query Params | Response | Auth Required | Roles |
|--------|----------|-------------|--------------|----------|---------------|-------|
| GET | /api/reviews/product/{productId} | Reviews của product | Path: productId, Params: page, size | Page<ReviewDto> | No | Public |
| GET | /api/reviews/product/{productId}/rating/{rating} | Filter by rating | Path: productId, rating, Params: page, size | Page<ReviewDto> | No | Public |
| GET | /api/reviews/product/{productId}/verified | Verified purchase reviews | Path: productId, Params: page, size | Page<ReviewDto> | No | Public |
| GET | /api/reviews/product/{productId}/helpful | Most helpful reviews | Path: productId, Params: page, size | Page<ReviewDto> | No | Public |
| GET | /api/reviews/product/{productId}/summary | Rating summary | Path: productId | ProductRatingSummary | No | Public |
| GET | /api/reviews/{reviewId} | Review details | Path: reviewId | ReviewDto | No | Public |

### 10.2 Customer Review APIs

| Method | Endpoint | Description | Request Body | Response | Auth Required | Roles |
|--------|----------|-------------|--------------|----------|---------------|-------|
| POST | /api/reviews | Tạo review | CreateReviewRequest | ReviewDto | Yes | CUSTOMER |
| PUT | /api/reviews/{reviewId} | Cập nhật review | UpdateReviewRequest | ReviewDto | Yes | CUSTOMER (owner) |
| DELETE | /api/reviews/{reviewId} | Xóa review | - | String message | Yes | CUSTOMER (owner) |
| GET | /api/reviews/my-reviews | Reviews của mình | Params: page, size | Page<ReviewDto> | Yes | CUSTOMER |
| POST | /api/reviews/{reviewId}/vote | Vote helpful/not helpful | Body: isHelpful (boolean) | ReviewDto | Yes | CUSTOMER |
| DELETE | /api/reviews/{reviewId}/vote | Xóa vote | - | ReviewDto | Yes | CUSTOMER |

### 10.3 Admin/Staff Review APIs

| Method | Endpoint | Description | Request Body | Response | Auth Required | Roles |
|--------|----------|-------------|--------------|----------|---------------|-------|
| GET | /api/reviews/status/{status} | Reviews theo status | Path: status, Params: page, size | Page<ReviewDto> | Yes | STAFF, ADMIN |
| GET | /api/reviews/pending | Pending reviews | Params: page, size | Page<ReviewDto> | Yes | STAFF, ADMIN |
| POST | /api/reviews/{reviewId}/approve | Duyệt review | - | ReviewDto | Yes | STAFF, ADMIN |
| POST | /api/reviews/{reviewId}/reject | Từ chối review | RejectReviewRequest (rejectionReason) | ReviewDto | Yes | STAFF, ADMIN |
| POST | /api/reviews/{reviewId}/reply | Reply review | ReplyReviewRequest (replyContent) | ReviewDto | Yes | SELLER, STAFF, ADMIN |

## 11. Recommendation APIs

| Method | Endpoint | Description | Query Params | Response | Auth Required | Roles |
|--------|----------|-------------|--------------|----------|---------------|-------|
| GET | /api/recommendations/homepage | Homepage recommendations | - | RecommendationDto | No | Public |
| GET | /api/recommendations/trending | Trending products | limit (default: 8) | RecommendationDto | No | Public |
| GET | /api/recommendations/best-sellers | Best sellers | limit (default: 8) | RecommendationDto | No | Public |
| GET | /api/recommendations/top-rated | Top rated | limit (default: 8) | RecommendationDto | No | Public |
| GET | /api/recommendations/new-arrivals | New arrivals | limit (default: 8) | RecommendationDto | No | Public |
| GET | /api/recommendations/similar/{productId} | Similar products | Path: productId, Params: limit (default: 8) | RecommendationDto | No | Public |
| GET | /api/recommendations/bought-together/{productId} | Frequently bought together | Path: productId, Params: limit (default: 5) | RecommendationDto | No | Public |
| GET | /api/recommendations/for-you | Personalized recommendations | Params: limit (default: 8) | RecommendationDto | Optional | CUSTOMER |

## 12. Cache Management APIs

| Method | Endpoint | Description | Request Body | Response | Auth Required | Roles |
|--------|----------|-------------|--------------|----------|---------------|-------|
| DELETE | /api/cache/clear | Clear all caches | - | String message | Yes | ADMIN |
| DELETE | /api/cache/clear/{cacheName} | Clear specific cache | Path: cacheName | String message | Yes | ADMIN |
| GET | /api/cache/stats | Cache statistics | - | Map<String, Object> | Yes | ADMIN |

---

## API Response Format

Tất cả API trả về theo format chuẩn:

### Success Response
```json
{
  "success": true,
  "message": "Success message",
  "data": { ... },
  "statusCode": 200,
  "timestamp": "2025-01-05T10:30:00"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error message",
  "error": {
    "code": "ERROR_CODE",
    "details": "Detailed error information"
  },
  "statusCode": 400,
  "timestamp": "2025-01-05T10:30:00"
}
```

## HTTP Status Codes

| Code | Meaning | Usage |
|------|---------|-------|
| 200 | OK | Request successful |
| 201 | Created | Resource created successfully |
| 204 | No Content | Request successful, no content to return |
| 400 | Bad Request | Invalid request data |
| 401 | Unauthorized | Missing or invalid authentication |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Duplicate resource or constraint violation |
| 500 | Internal Server Error | Server error |
| 503 | Service Unavailable | Service temporarily unavailable |

## Authentication

Tất cả protected endpoints yêu cầu JWT token trong header:

```
Authorization: Bearer <access_token>
```

Access token có thời hạn 15 phút. Sử dụng refresh token để lấy token mới khi hết hạn.

## Pagination

API hỗ trợ pagination với các tham số:

- `page`: Số trang (bắt đầu từ 0)
- `size`: Số lượng items mỗi trang (default: 20)
- `sort`: Trường sắp xếp (vd: "createdAt,desc")

Response có dạng:
```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalPages": 5,
  "totalElements": 100,
  "first": true,
  "last": false
}
```
