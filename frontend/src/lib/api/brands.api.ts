import { apiClient } from "./client";
import { Brand } from "../types";

export interface BrandRequest {
  name: string;
  description?: string;
  logoUrl?: string;
  website?: string;
  countryOfOrigin?: string;
  isFeatured?: boolean;
}

export const brandsApi = {
  // Get all brands (Public)
  getAllBrands: async (): Promise<Brand[]> => {
    const response = await apiClient.get<Brand[]>("/brands");
    return response.data;
  },

  // Get featured brands (Public)
  getFeaturedBrands: async (): Promise<Brand[]> => {
    const response = await apiClient.get<Brand[]>("/brands/featured");
    return response.data;
  },

  // Get brand by ID (Public)
  getBrandById: async (id: string): Promise<Brand> => {
    const response = await apiClient.get<Brand>(`/brands/${id}`);
    return response.data;
  },

  // Get brand by slug (Public)
  getBrandBySlug: async (slug: string): Promise<Brand> => {
    const response = await apiClient.get<Brand>(`/brands/slug/${slug}`);
    return response.data;
  },

  // Create brand (Admin only)
  createBrand: async (data: BrandRequest): Promise<Brand> => {
    const response = await apiClient.post<Brand>("/brands", data);
    return response.data;
  },

  // Update brand (Admin only)
  updateBrand: async (id: string, data: BrandRequest): Promise<Brand> => {
    const response = await apiClient.put<Brand>(`/brands/${id}`, data);
    return response.data;
  },

  // Delete brand (Admin only)
  deleteBrand: async (id: string): Promise<void> => {
    await apiClient.delete(`/brands/${id}`);
  },

  // Restore brand (Admin only)
  restoreBrand: async (id: string): Promise<Brand> => {
    const response = await apiClient.patch<Brand>(`/brands/${id}/restore`);
    return response.data;
  },
};
