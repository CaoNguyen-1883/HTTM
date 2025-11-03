import { useQuery, useMutation, useQueryClient, UseQueryOptions } from "@tanstack/react-query";
import { rolesApi } from "../api";
import { Role } from "../types";

// Query keys
export const roleQueryKeys = {
  all: ["roles"] as const,
  lists: () => [...roleQueryKeys.all, "list"] as const,
  details: () => [...roleQueryKeys.all, "detail"] as const,
  detail: (id: string) => [...roleQueryKeys.details(), id] as const,
};

// ===== QUERIES =====

/**
 * Get all roles
 */
export const useRoles = (options?: UseQueryOptions<Role[]>) => {
  return useQuery({
    queryKey: roleQueryKeys.lists(),
    queryFn: () => rolesApi.getAllRoles(),
    staleTime: 5 * 60 * 1000, // 5 minutes
    ...options,
  });
};

/**
 * Get role by ID
 */
export const useRole = (id: string, options?: UseQueryOptions<Role>) => {
  return useQuery({
    queryKey: roleQueryKeys.detail(id),
    queryFn: () => rolesApi.getRoleById(id),
    enabled: !!id,
    ...options,
  });
};

// ===== MUTATIONS =====

/**
 * Create new role
 */
export const useCreateRole = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: { name: string; description?: string; permissionIds?: string[] }) =>
      rolesApi.createRole(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: roleQueryKeys.lists() });
    },
  });
};

/**
 * Update role
 */
export const useUpdateRole = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      id,
      data,
    }: {
      id: string;
      data: { name?: string; description?: string; permissionIds?: string[] };
    }) => rolesApi.updateRole(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: roleQueryKeys.detail(variables.id) });
      queryClient.invalidateQueries({ queryKey: roleQueryKeys.lists() });
    },
  });
};

/**
 * Delete role
 */
export const useDeleteRole = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => rolesApi.deleteRole(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: roleQueryKeys.lists() });
    },
  });
};

/**
 * Restore role
 */
export const useRestoreRole = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => rolesApi.restoreRole(id),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: roleQueryKeys.detail(id) });
      queryClient.invalidateQueries({ queryKey: roleQueryKeys.lists() });
    },
  });
};

/**
 * Assign permissions to role
 */
export const useAssignPermissionsToRole = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ roleId, permissionIds }: { roleId: string; permissionIds: string[] }) =>
      rolesApi.assignPermissions(roleId, permissionIds),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: roleQueryKeys.detail(variables.roleId) });
      queryClient.invalidateQueries({ queryKey: roleQueryKeys.lists() });
    },
  });
};

/**
 * Remove permissions from role
 */
export const useRemovePermissionsFromRole = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ roleId, permissionIds }: { roleId: string; permissionIds: string[] }) =>
      rolesApi.removePermissions(roleId, permissionIds),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: roleQueryKeys.detail(variables.roleId) });
      queryClient.invalidateQueries({ queryKey: roleQueryKeys.lists() });
    },
  });
};