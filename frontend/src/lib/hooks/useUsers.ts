import { useQuery, useMutation, useQueryClient, UseQueryOptions } from "@tanstack/react-query";
import { usersApi } from "../api";
import {
  UserDetail,
  UserSummary,
  PageResponse,
  UserFilterParams,
  CreateUserRequest,
  UpdateUserRequest,
  ChangePasswordRequest,
  Role,
} from "../types";

// Query keys
export const userQueryKeys = {
  all: ["users"] as const,
  lists: () => [...userQueryKeys.all, "list"] as const,
  list: (params?: UserFilterParams) => [...userQueryKeys.lists(), params] as const,
  details: () => [...userQueryKeys.all, "detail"] as const,
  detail: (id: string) => [...userQueryKeys.details(), id] as const,
  search: (keyword: string, params?: any) => [...userQueryKeys.all, "search", keyword, params] as const,
  byRole: (roleName: string, params?: any) => [...userQueryKeys.all, "role", roleName, params] as const,
  roles: ["roles"] as const,
};

// ===== QUERIES =====

/**
 * Get all users (paginated)
 */
export const useUsers = (
  params?: UserFilterParams,
  options?: UseQueryOptions<PageResponse<UserSummary>>
) => {
  return useQuery({
    queryKey: userQueryKeys.list(params),
    queryFn: () => usersApi.getAllUsers(params),
    ...options,
  });
};

/**
 * Search users
 */
export const useSearchUsers = (
  keyword: string,
  params?: { page?: number; size?: number },
  options?: UseQueryOptions<PageResponse<UserSummary>>
) => {
  return useQuery({
    queryKey: userQueryKeys.search(keyword, params),
    queryFn: () => usersApi.searchUsers(keyword, params),
    enabled: keyword.length > 0,
    ...options,
  });
};

/**
 * Get users by role
 */
export const useUsersByRole = (
  roleName: string,
  params?: { page?: number; size?: number },
  options?: UseQueryOptions<PageResponse<UserSummary>>
) => {
  return useQuery({
    queryKey: userQueryKeys.byRole(roleName, params),
    queryFn: () => usersApi.getUsersByRole(roleName, params),
    enabled: !!roleName,
    ...options,
  });
};

/**
 * Get user detail by ID
 */
export const useUser = (
  id: string,
  options?: UseQueryOptions<UserDetail>
) => {
  return useQuery({
    queryKey: userQueryKeys.detail(id),
    queryFn: () => usersApi.getUserById(id),
    enabled: !!id,
    ...options,
  });
};

/**
 * Get all available roles
 */
export const useRoles = (options?: UseQueryOptions<Role[]>) => {
  return useQuery({
    queryKey: userQueryKeys.roles,
    queryFn: () => usersApi.getAllRoles(),
    staleTime: 10 * 60 * 1000, // 10 minutes
    ...options,
  });
};

// ===== MUTATIONS =====

/**
 * Create new user
 */
export const useCreateUser = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateUserRequest) => usersApi.createUser(data),
    onSuccess: () => {
      // Invalidate user lists
      queryClient.invalidateQueries({ queryKey: userQueryKeys.lists() });
    },
  });
};

/**
 * Update user
 */
export const useUpdateUser = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateUserRequest }) =>
      usersApi.updateUser(id, data),
    onSuccess: (_, variables) => {
      // Invalidate specific user and lists
      queryClient.invalidateQueries({ queryKey: userQueryKeys.detail(variables.id) });
      queryClient.invalidateQueries({ queryKey: userQueryKeys.lists() });
    },
  });
};

/**
 * Delete user
 */
export const useDeleteUser = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => usersApi.deleteUser(id),
    onSuccess: () => {
      // Invalidate user lists
      queryClient.invalidateQueries({ queryKey: userQueryKeys.lists() });
    },
  });
};

/**
 * Restore deleted user
 */
export const useRestoreUser = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => usersApi.restoreUser(id),
    onSuccess: (_, id) => {
      // Invalidate specific user and lists
      queryClient.invalidateQueries({ queryKey: userQueryKeys.detail(id) });
      queryClient.invalidateQueries({ queryKey: userQueryKeys.lists() });
    },
  });
};

/**
 * Assign roles to user
 */
export const useAssignRoles = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ userId, roleIds }: { userId: string; roleIds: string[] }) =>
      usersApi.assignRoles(userId, roleIds),
    onSuccess: (_, variables) => {
      // Invalidate specific user
      queryClient.invalidateQueries({ queryKey: userQueryKeys.detail(variables.userId) });
      queryClient.invalidateQueries({ queryKey: userQueryKeys.lists() });
    },
  });
};

/**
 * Remove roles from user
 */
export const useRemoveRoles = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ userId, roleIds }: { userId: string; roleIds: string[] }) =>
      usersApi.removeRoles(userId, roleIds),
    onSuccess: (_, variables) => {
      // Invalidate specific user
      queryClient.invalidateQueries({ queryKey: userQueryKeys.detail(variables.userId) });
      queryClient.invalidateQueries({ queryKey: userQueryKeys.lists() });
    },
  });
};

/**
 * Change password
 */
export const useChangePassword = () => {
  return useMutation({
    mutationFn: ({ userId, data }: { userId: string; data: ChangePasswordRequest }) =>
      usersApi.changePassword(userId, data),
  });
};

/**
 * Verify email
 */
export const useVerifyEmail = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (userId: string) => usersApi.verifyEmail(userId),
    onSuccess: (_, userId) => {
      // Invalidate specific user
      queryClient.invalidateQueries({ queryKey: userQueryKeys.detail(userId) });
      queryClient.invalidateQueries({ queryKey: userQueryKeys.lists() });
    },
  });
};