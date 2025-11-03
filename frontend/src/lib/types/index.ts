// ============================================
// COMMON TYPES
// ============================================



export interface ApiResponse<T = any> {
  success: boolean;
  message?: string;
  data?: T;
  error?: any;
  statusCode?: number;
  timestamp?: string;
  path?: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

// ============================================
// USER & AUTH TYPES
// ============================================

export enum UserRole {
  ROLE_ADMIN = "ROLE_ADMIN",
  ROLE_CUSTOMER = "ROLE_CUSTOMER",
  ROLE_SELLER = "ROLE_SELLER",
  ROLE_STAFF = "ROLE_STAFF",
}

export enum UserStatus {
  ACTIVE = "ACTIVE",
  INACTIVE = "INACTIVE",
  BANNED = "BANNED",
}

export interface User {
  id: string; // UUID
  email: string;
  fullName: string;
  avatarUrl?: string;
  roles: string[]; // Array of role names
  emailVerified: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
  expectedRole?: string; // NEW - Optional
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  phone?: string; // Changed from phoneNumber, optional
  // Removed: username, role (backend auto-assigns ROLE_CUSTOMER)
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user: User;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface CreatePermissionRequest {
  name: string;
  description?: string;
}

export interface UpdatePermissionRequest {
  name?: string;
  description?: string;
}

// Role Request Types
export interface CreateRoleRequest {
  name: string;
  description?: string;
  permissionIds?: string[]; // Array of Permission UUIDs
}

export interface UpdateRoleRequest {
  name?: string;
  description?: string;
  permissionIds?: string[]; // Array of Permission UUIDs
}


// ============================================
// ROLE & PERMISSION TYPES
// ============================================

// ============================================
// USER MANAGEMENT TYPES (Admin)
// ============================================

export enum AuthProvider {
  LOCAL = "LOCAL",
  GOOGLE = "GOOGLE",
  FACEBOOK = "FACEBOOK",
}

export interface Permission {
  id: string; // UUID
  name: string;
  description?: string;
}

export interface Role {
  id: string; // UUID
  name: string;
  description?: string;
  permissions?: Permission[];
  createdAt?: string;
  createdBy?: string;
}

// User Detail (full information)
export interface UserDetail {
  id: string; // UUID
  email: string;
  fullName: string;
  avatarUrl?: string;
  provider: AuthProvider;
  roles: Role[]; // Full role objects with permissions
  phone?: string;
  emailVerified: boolean;
  createdAt: string;
}

// User Summary (for list views)
export interface UserSummary {
  id: string; // UUID
  email: string;
  fullName: string;
  avatarUrl?: string;
  roleNames: string[]; // Just role names like ["ROLE_ADMIN", "ROLE_CUSTOMER"]
  emailVerified: boolean;
  createdAt: string;
}

// Create User Request
export interface CreateUserRequest {
  email: string;
  fullName: string;
  password: string;
  avatarUrl?: string;
  phone?: string;
  roleIds?: string[]; // UUID[]
}

// Update User Request
export interface UpdateUserRequest {
  email?: string;
  fullName?: string;
  avatarUrl?: string;
  phone?: string;
  roleIds?: string[]; // UUID[]
}

// Change Password Request
export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

// User Filter Params
export interface UserFilterParams {
  keyword?: string; // Search by email or name
  roleName?: string; // Filter by role
  page?: number;
  size?: number;
  sort?: string;
}

// Permission Request Types
export interface CreatePermissionRequest {
  name: string;
  description?: string;
}

export interface UpdatePermissionRequest {
  name?: string;
  description?: string;
}

// Role Request Types  
export interface CreateRoleRequest {
  name: string;
  description?: string;
  permissionIds?: string[]; // Array of Permission UUIDs
}

export interface UpdateRoleRequest {
  name?: string;
  description?: string;
  permissionIds?: string[]; // Array of Permission UUIDs
}

// ============================================
// PRODUCT TYPES
// ============================================

export enum ProductStatus {
  PENDING = "PENDING",           // Waiting for staff approval
  APPROVED = "APPROVED",         // Staff approved, visible to customers
  REJECTED = "REJECTED",         // Staff rejected
  ACTIVE = "ACTIVE",             // Currently selling
  INACTIVE = "INACTIVE",         // Temporarily not selling
  OUT_OF_STOCK = "OUT_OF_STOCK"  // All variants out of stock
}

export interface Category {
  id: string;
  name: string;
  description?: string;
  slug: string;
  imageUrl?: string;
  parentId?: string;
  parentName?: string;       
  children?: Category[];      
  displayOrder?: number;     
  createdAt?: string;         
}

export interface Brand {
  id: string;
  name: string;
  description?: string;
  slug: string;
  logoUrl?: string;
  website?: string;          
  countryOfOrigin?: string;  
  isFeatured?: boolean;     
  createdAt?: string;       
}

export interface ProductImage {
  id: string;
  imageUrl: string;     
  altText?: string;      
  isPrimary: boolean;
  displayOrder: number;
}

export interface ProductVariant {
  id: string;
  sku: string;
  name: string;
  price: number;                       
  stock: number;                        
  availableStock: number;              
  specifications?: Record<string, any>; 
  attributes: Record<string, any>;     
  isDefault: boolean;                  
}

// For Product List (summary)
export interface ProductSummary {
  id: string; // UUID
  name: string;
  slug: string;
  shortDescription?: string;
  categoryName: string;
  brandName: string;
  basePrice: number;
  minPrice: number;
  maxPrice: number;
  status: ProductStatus;
  primaryImage: string;
  averageRating: number;
  totalReviews: number;
  totalStock: number;
  hasStock: boolean;
  defaultVariantId?: string; // First available variant ID for quick add to cart
}

// For Product Detail
export interface Product {
  id: string; // UUID
  name: string;
  slug: string;
  description: string;
  shortDescription?: string;
  category: Category;
  brand: Brand;
  sellerId: string;
  sellerName: string;
  basePrice: number;
  status: ProductStatus;
  tags: string[];
  viewCount: number;
  purchaseCount: number;
  averageRating: number;
  totalReviews: number;
  totalStock: number;
  variants: ProductVariant[];
  images: ProductImage[];
  createdAt: string;
  approvedAt?: string;
}

export interface ProductFilterParams {
  keyword?: string;
  categoryId?: string;
  brandId?: string;
  minPrice?: number;
  maxPrice?: number;
  status?: ProductStatus;
  page?: number;
  size?: number;
  sort?: string;
}


// ============================================
// CART TYPES
// ============================================

export interface CartItem {
  id: string;                 
  variantId: string;          
  variantSku: string;
  variantName: string;
  productName: string;
  productId: string;          
  productSlug: string;        
  primaryImage: string;       
  priceAtAdd: number;          
  quantity: number;
  subtotal: number;
  availableStock: number;      
  isAvailable: boolean;       
}

export interface Cart {
  id: string;
  userId: string;
  items: CartItem[];
  totalItems: number;
  subtotal: number;
  discount: number;
  totalPrice: number;
  updatedAt: string;
}
export interface AddToCartRequest {
  variantId: string;         
  quantity: number;
}

export interface UpdateCartItemRequest {
  quantity: number;
}

// ============================================
// ORDER TYPES
// ============================================

export enum OrderStatus {
  PENDING = "PENDING",
  CONFIRMED = "CONFIRMED",
  PROCESSING = "PROCESSING",
  SHIPPED = "SHIPPED",
  DELIVERED = "DELIVERED",
  CANCELLED = "CANCELLED",
}

export enum PaymentStatus {
  PENDING = "PENDING",
  PAID = "PAID",
  FAILED = "FAILED",
  REFUNDED = "REFUNDED",
}

export enum PaymentMethod {
  COD = "COD",
  BANK_TRANSFER = "BANK_TRANSFER",
  CREDIT_CARD = "CREDIT_CARD",
  E_WALLET = "E_WALLET",
}

export interface OrderItem {
  id: string;
  productId: string;
  productName: string;
  variantId: string;
  variantName: string;
  variantSku: string;
  variantImageUrl?: string;
  price: number;
  quantity: number;
  subtotal: number;
}

export interface Order {
  id: string;
  orderNumber: string;
  userId: string;
  userFullName: string;
  userEmail: string;
  items: OrderItem[];

  // Pricing
  subtotal: number;
  shippingFee: number;
  tax: number;
  discount: number;
  totalAmount: number;

  // Shipping
  shippingAddress: string;
  shippingCity: string;
  shippingDistrict?: string;
  shippingWard?: string;
  shippingPhone: string;
  shippingRecipient: string;

  // Billing
  billingAddress?: string;
  billingCity?: string;
  billingDistrict?: string;
  billingWard?: string;

  // Payment & Status
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  orderStatus: OrderStatus;

  // Notes
  notes?: string;
  cancelReason?: string;
  adminNotes?: string;

  // Timestamps
  confirmedAt?: string;
  shippedAt?: string;
  deliveredAt?: string;
  cancelledAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateOrderRequest {
  shippingAddress: string;
  shippingCity: string;
  shippingDistrict?: string;
  shippingWard?: string;
  shippingPhone: string;
  shippingRecipient: string;
  billingAddress?: string;
  billingCity?: string;
  billingDistrict?: string;
  billingWard?: string;
  paymentMethod: PaymentMethod;
  notes?: string;
}

export interface CancelOrderRequest {
  orderId: string;
  reason: string;
}

// ============================================
// REVIEW TYPES
// ============================================

export enum ReviewStatus {
  PENDING = "PENDING",
  APPROVED = "APPROVED",
  REJECTED = "REJECTED",
}

export interface ReviewImage {
  id: string;
  imageUrl: string;
  displayOrder: number;
}

export interface Review {
  id: string;
  productId: string;
  productName: string;
  userId: string;
  userName: string;
  userAvatar?: string;
  orderId?: string;
  rating: number;
  title: string;
  content: string;
  images: ReviewImage[];
  status: ReviewStatus;
  isVerifiedPurchase: boolean;
  helpfulCount: number;
  notHelpfulCount: number;
  totalVotes: number;
  helpfulPercentage: number;

  // User's vote (if authenticated)
  userVote?: boolean | null;  // null = no vote, true = helpful, false = not helpful

  // Reply
  replyContent?: string;
  repliedAt?: string;
  repliedByName?: string;

  createdAt: string;
  updatedAt: string;
  approvedAt?: string;
}

export interface CreateReviewRequest {
  productId: string;
  orderId?: string;
  rating: number;
  title: string;
  content: string;
  imageUrls?: string[];
}

export interface UpdateReviewRequest {
  rating: number;
  title: string;
  content: string;
  imageUrls?: string[];
}

export interface ProductRatingSummary {
  productId: string;
  averageRating: number;
  totalReviews: number;
  fiveStarCount: number;
  fourStarCount: number;
  threeStarCount: number;
  twoStarCount: number;
  oneStarCount: number;
  fiveStarPercentage: number;
  fourStarPercentage: number;
  threeStarPercentage: number;
  twoStarPercentage: number;
  oneStarPercentage: number;
}

export interface ReplyReviewRequest {
  content: string;
}

export interface RejectReviewRequest {
  reason: string;
}

export interface ReviewFilterParams {
  productId?: string;
  userId?: string;
  rating?: number;
  status?: ReviewStatus;
  isVerifiedPurchase?: boolean;
  page?: number;
  size?: number;
  sort?: string;
}

// ============================================
// STATISTICS TYPES (for Admin/Seller)
// ============================================

export interface RevenueStats {
  totalRevenue: number;
  totalOrders: number;
  averageOrderValue: number;
  period: string;
}

export interface ProductStats {
  totalProducts: number;
  activeProducts: number;
  outOfStockProducts: number;
  totalVariants: number;
}

export interface OrderStats {
  pending: number;
  confirmed: number;
  processing: number;
  shipped: number;
  delivered: number;
  cancelled: number;
}

// ============================================
// FILTER & SEARCH PARAMS
// ============================================

// export interface ProductFilterParams {
//   categoryId?: number;
//   sellerId?: number;
//   minPrice?: number;
//   maxPrice?: number;
//   status?: ProductStatus;
//   search?: string;
//   page?: number;
//   size?: number;
//   sort?: string;
// }

export interface OrderFilterParams {
  userId?: string;
  orderStatus?: OrderStatus;
  paymentStatus?: PaymentStatus;
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
  sort?: string;
}