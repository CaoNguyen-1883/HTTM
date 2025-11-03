import { apiClient } from "./client";
import { Category } from "../types";

export interface CategoryRequest {
  name: string;
  description?: string;
  parentId?: string;
  imageUrl?: string;
  displayOrder?: number;
}

export const categoriesApi = {
  // Get all categories (Public)
  getAllCategories: async (): Promise<Category[]> => {
    const response = await apiClient.get<Category[]>("/categories");
    return response.data;
  },

  // Get root categories (Public)
  getRootCategories: async (): Promise<Category[]> => {
    const response = await apiClient.get<Category[]>("/categories/root");
    return response.data;
  },

  // Get category by ID (Public)
  getCategoryById: async (id: string): Promise<Category> => {
    const response = await apiClient.get<Category>(`/categories/${id}`);
    return response.data;
  },

  // Get category by slug (Public)
  getCategoryBySlug: async (slug: string): Promise<Category> => {
    const response = await apiClient.get<Category>(`/categories/slug/${slug}`);
    return response.data;
  },

  // Get sub-categories (Public)
  getSubCategories: async (parentId: string): Promise<Category[]> => {
    const response = await apiClient.get<Category[]>(`/categories/${parentId}/children`);
    return response.data;
  },

  // Create category (Admin only)
  createCategory: async (data: CategoryRequest): Promise<Category> => {
    const response = await apiClient.post<Category>("/categories", data);
    return response.data;
  },

  // Update category (Admin only)
  updateCategory: async (id: string, data: CategoryRequest): Promise<Category> => {
    const response = await apiClient.put<Category>(`/categories/${id}`, data);
    return response.data;
  },

  // Delete category (Admin only)
  deleteCategory: async (id: string): Promise<void> => {
    await apiClient.delete(`/categories/${id}`);
  },

  // Restore category (Admin only)
  restoreCategory: async (id: string): Promise<Category> => {
    const response = await apiClient.patch<Category>(`/categories/${id}/restore`);
    return response.data;
  },
};
