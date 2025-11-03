import { OrderStatus, PaymentStatus, PaymentMethod, ReviewStatus } from "../types";

// API Configuration
export const API_CONFIG = {
  BASE_URL: "/api",
  TIMEOUT: 30000,
  TOKEN_KEY: "access_token",
  REFRESH_TOKEN_KEY: "refresh_token",
};

// Pagination
export const PAGINATION = {
  DEFAULT_PAGE: 0,
  DEFAULT_SIZE: 20,
  SIZE_OPTIONS: [10, 20, 50, 100],
};

// Order Status Labels
export const ORDER_STATUS_LABELS: Record<OrderStatus, string> = {
  [OrderStatus.PENDING]: "Chờ xác nhận",
  [OrderStatus.CONFIRMED]: "Đã xác nhận",
  [OrderStatus.PROCESSING]: "Đang xử lý",
  [OrderStatus.SHIPPED]: "Đang giao",
  [OrderStatus.DELIVERED]: "Đã giao",
  [OrderStatus.CANCELLED]: "Đã hủy",
};

// Order Status Colors (for badges)
export const ORDER_STATUS_COLORS: Record<OrderStatus, string> = {
  [OrderStatus.PENDING]: "bg-yellow-100 text-yellow-800",
  [OrderStatus.CONFIRMED]: "bg-blue-100 text-blue-800",
  [OrderStatus.PROCESSING]: "bg-purple-100 text-purple-800",
  [OrderStatus.SHIPPED]: "bg-indigo-100 text-indigo-800",
  [OrderStatus.DELIVERED]: "bg-green-100 text-green-800",
  [OrderStatus.CANCELLED]: "bg-red-100 text-red-800",
};

// Payment Status Labels
export const PAYMENT_STATUS_LABELS: Record<PaymentStatus, string> = {
  [PaymentStatus.PENDING]: "Chờ thanh toán",
  [PaymentStatus.PAID]: "Đã thanh toán",
  [PaymentStatus.FAILED]: "Thanh toán thất bại",
  [PaymentStatus.REFUNDED]: "Đã hoàn tiền",
};

// Payment Method Labels
export const PAYMENT_METHOD_LABELS: Record<PaymentMethod, string> = {
  [PaymentMethod.COD]: "Thanh toán khi nhận hàng (COD)",
  [PaymentMethod.BANK_TRANSFER]: "Chuyển khoản ngân hàng",
  [PaymentMethod.CREDIT_CARD]: "Thẻ tín dụng",
  [PaymentMethod.E_WALLET]: "Ví điện tử",
};

// Review Status Labels
export const REVIEW_STATUS_LABELS: Record<ReviewStatus, string> = {
  [ReviewStatus.PENDING]: "Chờ duyệt",
  [ReviewStatus.APPROVED]: "Đã duyệt",
  [ReviewStatus.REJECTED]: "Đã từ chối",
};

// Rating Stars
export const RATING_STARS = [1, 2, 3, 4, 5];

// Image Upload
export const IMAGE_CONFIG = {
  MAX_SIZE: 5 * 1024 * 1024, // 5MB
  ALLOWED_TYPES: ["image/jpeg", "image/png", "image/webp"],
  MAX_FILES: 10,
};

// Toast Duration
export const TOAST_DURATION = {
  SUCCESS: 3000,
  ERROR: 5000,
  INFO: 3000,
};

// Debounce Delays
export const DEBOUNCE_DELAY = {
  SEARCH: 500,
  INPUT: 300,
};