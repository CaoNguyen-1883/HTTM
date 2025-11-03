import { useQuery, useMutation, useQueryClient, UseQueryOptions } from "@tanstack/react-query";
import { permissionsApi } from "../api";
import { Permission } from "../types";

// Query keys
export const permissionQueryKeys = {
  all: ["permissions"] as const,
  lists: () => [...permissionQueryKeys.all, "list"] as const,
  details: () => [...permissionQueryKeys.all, "detail"] as const,
  detail: (id: string) => [...permissionQueryKeys.details(), id] as const,
};

// ===== QUERIES =====

/**
 * Get all permissions
 */
export const usePermissions = (options?: UseQueryOptions<Permission[]>) => {
  return useQuery({
    queryKey: permissionQueryKeys.lists(),
    queryFn: () => permissionsApi.getAllPermissions(),
    staleTime: 10 * 60 * 1000, // 10 minutes
    ...options,
  });
};

/**
 * Get permission by ID
 */
export const usePermission = (id: string, options?: UseQueryOptions<Permission>) => {
  return useQuery({
    queryKey: permissionQueryKeys.detail(id),
    queryFn: () => permissionsApi.getPermissionById(id),
    enabled: !!id,
    ...options,
  });
};

// ===== MUTATIONS =====

/**
 * Create new permission
 */
export const useCreatePermission = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: { name: string; description?: string }) =>
      permissionsApi.createPermission(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: permissionQueryKeys.lists() });
    },
  });
};

/**
 * Update permission
 */
export const useUpdatePermission = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      id,
      data,
    }: {
      id: string;
      data: { name?: string; description?: string };
    }) => permissionsApi.updatePermission(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: permissionQueryKeys.detail(variables.id) });
      queryClient.invalidateQueries({ queryKey: permissionQueryKeys.lists() });
    },
  });
};

/**
 * Delete permission
 */
export const useDeletePermission = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => permissionsApi.deletePermission(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: permissionQueryKeys.lists() });
    },
  });
};

/**
 * Restore permission
 */
export const useRestorePermission = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => permissionsApi.restorePermission(id),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: permissionQueryKeys.detail(id) });
      queryClient.invalidateQueries({ queryKey: permissionQueryKeys.lists() });
    },
  });
};