import { useQuery, useMutation, useQueryClient, UseQueryOptions } from "@tanstack/react-query";
import { brandsApi, BrandRequest } from "../api/brands.api";
import { Brand } from "../types";

// Query keys
export const brandKeys = {
  all: ["brands"] as const,
  lists: () => [...brandKeys.all, "list"] as const,
  list: () => [...brandKeys.lists()] as const,
  featured: () => [...brandKeys.all, "featured"] as const,
  details: () => [...brandKeys.all, "detail"] as const,
  detail: (id: string) => [...brandKeys.details(), id] as const,
};

// Get all brands
export const useBrands = (options?: UseQueryOptions<Brand[]>) => {
  return useQuery({
    queryKey: brandKeys.list(),
    queryFn: () => brandsApi.getAllBrands(),
    staleTime: 10 * 60 * 1000, // 10 minutes
    ...options,
  });
};

// Get featured brands
export const useFeaturedBrands = (options?: UseQueryOptions<Brand[]>) => {
  return useQuery({
    queryKey: brandKeys.featured(),
    queryFn: () => brandsApi.getFeaturedBrands(),
    staleTime: 10 * 60 * 1000,
    ...options,
  });
};

// Get brand by ID
export const useBrand = (id: string, options?: UseQueryOptions<Brand>) => {
  return useQuery({
    queryKey: brandKeys.detail(id),
    queryFn: () => brandsApi.getBrandById(id),
    enabled: !!id,
    ...options,
  });
};

// Create brand mutation
export const useCreateBrand = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: BrandRequest) => brandsApi.createBrand(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: brandKeys.all });
    },
  });
};

// Update brand mutation
export const useUpdateBrand = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: BrandRequest }) =>
      brandsApi.updateBrand(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: brandKeys.all });
    },
  });
};

// Delete brand mutation
export const useDeleteBrand = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => brandsApi.deleteBrand(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: brandKeys.all });
    },
  });
};

// Restore brand mutation
export const useRestoreBrand = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => brandsApi.restoreBrand(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: brandKeys.all });
    },
  });
};
