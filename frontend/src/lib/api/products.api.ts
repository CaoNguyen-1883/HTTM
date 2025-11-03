import { apiClient } from "./client";
import {
  Product,
  ProductSummary,
  PageResponse,
  ProductFilterParams,
  Category,
  Brand,
} from "../types";

export const productsApi = {
  // Get all products (paginated)
  getProducts: async (
    params?: ProductFilterParams
  ): Promise<PageResponse<ProductSummary>> => {
    const { page = 0, size = 20, sort = "createdAt,desc", ...filters } = params || {};
    
    const response = await apiClient.get<PageResponse<ProductSummary>>("/products", {
      params: {
        page,
        size,
        sort,
        ...filters,
      },
    });
    return response.data;
  },

  // Search products
  searchProducts: async (
    keyword: string,
    params?: { page?: number; size?: number }
  ): Promise<PageResponse<ProductSummary>> => {
    const { page = 0, size = 20 } = params || {};
    
    const response = await apiClient.get<PageResponse<ProductSummary>>(
      "/products/search",
      {
        params: { keyword, page, size },
      }
    );
    return response.data;
  },

  // Get products by category
  getProductsByCategory: async (
    categoryId: string,
    params?: { page?: number; size?: number }
  ): Promise<PageResponse<ProductSummary>> => {
    const { page = 0, size = 20 } = params || {};
    
    const response = await apiClient.get<PageResponse<ProductSummary>>(
      `/products/category/${categoryId}`,
      {
        params: { page, size },
      }
    );
    return response.data;
  },

  // Get products by brand
  getProductsByBrand: async (
    brandId: string,
    params?: { page?: number; size?: number }
  ): Promise<PageResponse<ProductSummary>> => {
    const { page = 0, size = 20 } = params || {};
    
    const response = await apiClient.get<PageResponse<ProductSummary>>(
      `/products/brand/${brandId}`,
      {
        params: { page, size },
      }
    );
    return response.data;
  },

  // Get product by ID
  getProductById: async (id: string): Promise<Product> => {
    const response = await apiClient.get<Product>(`/products/${id}`);
    return response.data;
  },

  // Get product by slug (if backend supports)
  getProductBySlug: async (slug: string): Promise<Product> => {
    // Note: Backend might not have this endpoint, using ID for now
    const response = await apiClient.get<Product>(`/products/slug/${slug}`);
    return response.data;
  },

  // Get all categories
  getCategories: async (): Promise<Category[]> => {
    const response = await apiClient.get<Category[]>("/categories");
    return response.data;
  },

  // Get all brands
  getBrands: async (): Promise<Brand[]> => {
    const response = await apiClient.get<Brand[]>("/brands");
    return response.data;
  },

  // ===== SELLER ENDPOINTS =====

  // Create product (Seller)
  createProduct: async (data: any): Promise<Product> => {
    const response = await apiClient.post<Product>("/products", data);
    return response.data;
  },

  // Get seller's products
  getMyProducts: async (params?: ProductFilterParams): Promise<PageResponse<ProductSummary>> => {
    const { page = 0, size = 20, sort = "createdAt,desc", ...filters } = params || {};

    const response = await apiClient.get<PageResponse<ProductSummary>>("/products/my-products", {
      params: {
        page,
        size,
        sort,
        ...filters,
      },
    });
    return response.data;
  },

  // Update product (Seller)
  updateProduct: async (id: string, data: any): Promise<Product> => {
    const response = await apiClient.put<Product>(`/products/${id}`, data);
    return response.data;
  },

  // Delete product (Seller)
  deleteProduct: async (id: string): Promise<void> => {
    await apiClient.delete(`/products/${id}`);
  },

  // ===== STAFF/ADMIN ENDPOINTS =====

  // Get pending products
  getPendingProducts: async (params?: { page?: number; size?: number }): Promise<PageResponse<ProductSummary>> => {
    const { page = 0, size = 20 } = params || {};

    const response = await apiClient.get<PageResponse<ProductSummary>>("/products/pending", {
      params: { page, size },
    });
    return response.data;
  },

  // Get products by status
  getProductsByStatus: async (
    status: string,
    params?: { page?: number; size?: number; sort?: string; keyword?: string }
  ): Promise<PageResponse<ProductSummary>> => {
    const { page = 0, size = 20, sort = "createdAt,desc", keyword } = params || {};

    const response = await apiClient.get<PageResponse<ProductSummary>>(
      `/products/status/${status}`,
      {
        params: { page, size, sort, keyword },
      }
    );
    return response.data;
  },

  // Get all products for admin/staff (regardless of status)
  getAllProductsForAdmin: async (
    params?: ProductFilterParams
  ): Promise<PageResponse<ProductSummary>> => {
    const { page = 0, size = 20, sort = "createdAt,desc", status, keyword } = params || {};

    // If status is specified, use the status endpoint
    if (status) {
      return productsApi.getProductsByStatus(status, { page, size, sort, keyword });
    }

    // Otherwise, get ALL products regardless of status
    const response = await apiClient.get<PageResponse<ProductSummary>>("/products/admin/all", {
      params: { page, size, sort, keyword },
    });
    return response.data;
  },

  // Approve product (Staff/Admin)
  approveProduct: async (id: string): Promise<Product> => {
    const response = await apiClient.post<Product>(`/products/${id}/approve`);
    return response.data;
  },

  // Reject product (Staff/Admin)
  rejectProduct: async (id: string, reason: string): Promise<Product> => {
    const response = await apiClient.post<Product>(`/products/${id}/reject`, { reason });
    return response.data;
  },

  // Get trending products
  getTrendingProducts: async (params?: { page?: number; size?: number }): Promise<PageResponse<ProductSummary>> => {
    const { page = 0, size = 10 } = params || {};

    const response = await apiClient.get<PageResponse<ProductSummary>>("/products/trending", {
      params: { page, size },
    });
    return response.data;
  },

  // Get best sellers
  getBestSellers: async (params?: { page?: number; size?: number }): Promise<PageResponse<ProductSummary>> => {
    const { page = 0, size = 10 } = params || {};

    const response = await apiClient.get<PageResponse<ProductSummary>>("/products/best-sellers", {
      params: { page, size },
    });
    return response.data;
  },

  // Get top rated
  getTopRated: async (params?: { page?: number; size?: number }): Promise<PageResponse<ProductSummary>> => {
    const { page = 0, size = 10 } = params || {};

    const response = await apiClient.get<PageResponse<ProductSummary>>("/products/top-rated", {
      params: { page, size },
    });
    return response.data;
  },
};