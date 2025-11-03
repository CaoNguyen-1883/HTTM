package dev.CaoNguyen_1883.ecommerce.seed.constants;

public final class PermissionConstants {
    private PermissionConstants() {}

    // User permissions
    public static final String USER_VIEW = "user:view";
    public static final String USER_CREATE = "user:create";
    public static final String USER_UPDATE = "user:update";
    public static final String USER_DELETE = "user:delete";

    // Product permissions
    public static final String PRODUCT_VIEW = "product:view";
    public static final String PRODUCT_CREATE = "product:create";
    public static final String PRODUCT_UPDATE = "product:update";
    public static final String PRODUCT_DELETE = "product:delete";

    // Order permissions
    public static final String ORDER_VIEW = "order:view";
    public static final String ORDER_UPDATE = "order:update";
    public static final String ORDER_APPROVE = "order:approve";
    public static final String ORDER_CANCEL = "order:cancel";

    // Category permissions
    public static final String CATEGORY_VIEW = "category:view";
    public static final String CATEGORY_CREATE = "category:create";
    public static final String CATEGORY_UPDATE = "category:update";
    public static final String CATEGORY_DELETE = "category:delete";

    // Brand permissions
    public static final String BRAND_VIEW = "brand:view";
    public static final String BRAND_CREATE = "brand:create";
    public static final String BRAND_UPDATE = "brand:update";
    public static final String BRAND_DELETE = "brand:delete";

    // Review permissions
    public static final String REVIEW_VIEW = "review:view";
    public static final String REVIEW_CREATE = "review:create";
    public static final String REVIEW_DELETE = "review:delete";

    // Payment permissions
    public static final String PAYMENT_VIEW = "payment:view";
    public static final String PAYMENT_UPDATE = "payment:update";

    // Analytics permissions
    public static final String RECOMMENDATION_VIEW = "recommendation:view";
    public static final String ANALYTICS_VIEW = "analytics:view";
}
