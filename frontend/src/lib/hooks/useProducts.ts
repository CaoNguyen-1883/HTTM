import { useQuery, useMutation, useQueryClient, UseQueryOptions } from "@tanstack/react-query";
import { productsApi } from "../api";
import { queryKeys } from "../queryClient";
import {
  Product,
  ProductSummary,
  PageResponse,
  ProductFilterParams,
} from "../types";

// Get products list
export const useProducts = (
  params?: ProductFilterParams,
  options?: UseQueryOptions<PageResponse<ProductSummary>>
) => {
  return useQuery({
    queryKey: queryKeys.products.list(params),
    queryFn: () => productsApi.getProducts(params),
    ...options,
  });
};

// Search products
export const useSearchProducts = (
  keyword: string,
  params?: { page?: number; size?: number },
  options?: UseQueryOptions<PageResponse<ProductSummary>>
) => {
  return useQuery({
    queryKey: queryKeys.products.search(keyword, params),
    queryFn: () => productsApi.searchProducts(keyword, params),
    enabled: keyword.length > 0, // Only search if keyword exists
    ...options,
  });
};

// Get product detail
export const useProduct = (
  id: string,
  options?: UseQueryOptions<Product>
) => {
  return useQuery({
    queryKey: queryKeys.products.detail(id),
    queryFn: () => productsApi.getProductById(id),
    enabled: !!id,
    ...options,
  });
};

// ===== SELLER HOOKS =====

// Get seller's products
export const useMyProducts = (
  params?: ProductFilterParams,
  options?: UseQueryOptions<PageResponse<ProductSummary>>
) => {
  return useQuery({
    queryKey: ["products", "my-products", params],
    queryFn: () => productsApi.getMyProducts(params),
    ...options,
  });
};

// Create product mutation
export const useCreateProduct = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: any) => productsApi.createProduct(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["products"] });
    },
  });
};

// Update product mutation
export const useUpdateProduct = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: any }) =>
      productsApi.updateProduct(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["products"] });
    },
  });
};

// Delete product mutation
export const useDeleteProduct = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => productsApi.deleteProduct(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["products"] });
    },
  });
};

// ===== STAFF/ADMIN HOOKS =====

// Get pending products
export const usePendingProducts = (
  params?: { page?: number; size?: number },
  options?: UseQueryOptions<PageResponse<ProductSummary>>
) => {
  return useQuery({
    queryKey: ["products", "pending", params],
    queryFn: () => productsApi.getPendingProducts(params),
    ...options,
  });
};

// Get products by status
export const useProductsByStatus = (
  status: string,
  params?: { page?: number; size?: number },
  options?: UseQueryOptions<PageResponse<ProductSummary>>
) => {
  return useQuery({
    queryKey: ["products", "status", status, params],
    queryFn: () => productsApi.getProductsByStatus(status, params),
    enabled: !!status,
    ...options,
  });
};

// Get all products for admin/staff (with status filtering)
export const useAdminProducts = (
  params?: ProductFilterParams,
  options?: UseQueryOptions<PageResponse<ProductSummary>>
) => {
  return useQuery({
    queryKey: ["products", "admin", params],
    queryFn: () => productsApi.getAllProductsForAdmin(params),
    ...options,
  });
};

// Approve product mutation
export const useApproveProduct = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => productsApi.approveProduct(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["products"] });
    },
  });
};

// Reject product mutation
export const useRejectProduct = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) =>
      productsApi.rejectProduct(id, reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["products"] });
    },
  });
};

// // ===== PUBLIC HOOKS =====

// // Get trending products
// export const useTrendingProducts = (
//   params?: { page?: number; size?: number },
//   options?: UseQueryOptions<PageResponse<ProductSummary>>
// ) => {
//   return useQuery({
//     queryKey: ["products", "trending", params],
//     queryFn: () => productsApi.getTrendingProducts(params),
//     ...options,
//   });
// };

// // Get best sellers
// export const useBestSellers = (
//   params?: { page?: number; size?: number },
//   options?: UseQueryOptions<PageResponse<ProductSummary>>
// ) => {
//   return useQuery({
//     queryKey: ["products", "best-sellers", params],
//     queryFn: () => productsApi.getBestSellers(params),
//     ...options,
//   });
// };

// // Get top rated
// export const useTopRated = (
//   params?: { page?: number; size?: number },
//   options?: UseQueryOptions<PageResponse<ProductSummary>>
// ) => {
//   return useQuery({
//     queryKey: ["products", "top-rated", params],
//     queryFn: () => productsApi.getTopRated(params),
//     ...options,
//   });
// };