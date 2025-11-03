import { useQuery, useMutation, useQueryClient, UseQueryOptions } from "@tanstack/react-query";
import { categoriesApi, CategoryRequest } from "../api/categories.api";
import { Category } from "../types";

// Query keys
export const categoryKeys = {
  all: ["categories"] as const,
  lists: () => [...categoryKeys.all, "list"] as const,
  list: () => [...categoryKeys.lists()] as const,
  roots: () => [...categoryKeys.all, "roots"] as const,
  details: () => [...categoryKeys.all, "detail"] as const,
  detail: (id: string) => [...categoryKeys.details(), id] as const,
  children: (parentId: string) => [...categoryKeys.all, "children", parentId] as const,
};

// Get all categories
export const useAllCategories = (options?: UseQueryOptions<Category[]>) => {
  return useQuery({
    queryKey: categoryKeys.list(),
    queryFn: () => categoriesApi.getAllCategories(),
    staleTime: 10 * 60 * 1000, // 10 minutes
    ...options,
  });
};

// Get root categories
export const useRootCategories = (options?: UseQueryOptions<Category[]>) => {
  return useQuery({
    queryKey: categoryKeys.roots(),
    queryFn: () => categoriesApi.getRootCategories(),
    staleTime: 10 * 60 * 1000,
    ...options,
  });
};

// Get category by ID
export const useCategory = (id: string, options?: UseQueryOptions<Category>) => {
  return useQuery({
    queryKey: categoryKeys.detail(id),
    queryFn: () => categoriesApi.getCategoryById(id),
    enabled: !!id,
    ...options,
  });
};

// Get sub-categories
export const useSubCategories = (parentId: string, options?: UseQueryOptions<Category[]>) => {
  return useQuery({
    queryKey: categoryKeys.children(parentId),
    queryFn: () => categoriesApi.getSubCategories(parentId),
    enabled: !!parentId,
    ...options,
  });
};

// Create category mutation
export const useCreateCategory = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CategoryRequest) => categoriesApi.createCategory(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: categoryKeys.all });
    },
  });
};

// Update category mutation
export const useUpdateCategory = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: CategoryRequest }) =>
      categoriesApi.updateCategory(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: categoryKeys.all });
    },
  });
};

// Delete category mutation
export const useDeleteCategory = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => categoriesApi.deleteCategory(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: categoryKeys.all });
    },
  });
};

// Restore category mutation
export const useRestoreCategory = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => categoriesApi.restoreCategory(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: categoryKeys.all });
    },
  });
};
