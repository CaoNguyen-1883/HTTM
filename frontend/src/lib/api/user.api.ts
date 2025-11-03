import { apiClient } from "./client";
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

export const usersApi = {
  /**
   * Get all users (paginated)
   * @requires ROLE_ADMIN
   */
  getAllUsers: async (params?: UserFilterParams): Promise<PageResponse<UserSummary>> => {
    const { page = 0, size = 20, sort = "createdAt,desc", ...filters } = params || {};
    const response = await apiClient.get<PageResponse<UserSummary>>("/users", {
      params: { page, size, sort, ...filters },
    });
    return response.data;
  },

  /**
   * Search users by email or name
   * @requires ROLE_ADMIN
   */
  searchUsers: async (
    keyword: string,
    params?: { page?: number; size?: number }
  ): Promise<PageResponse<UserSummary>> => {
    const { page = 0, size = 20 } = params || {};
    const response = await apiClient.get<PageResponse<UserSummary>>("/users/search", {
      params: { keyword, page, size },
    });
    return response.data;
  },

  /**
   * Get users by role name
   * @requires ROLE_ADMIN
   */
  getUsersByRole: async (
    roleName: string,
    params?: { page?: number; size?: number }
  ): Promise<PageResponse<UserSummary>> => {
    const { page = 0, size = 20 } = params || {};
    const response = await apiClient.get<PageResponse<UserSummary>>(
      `/users/role/${roleName}`,
      {
        params: { page, size },
      }
    );
    return response.data;
  },

  /**
   * Get user by ID
   * @requires ROLE_ADMIN or ROLE_STAFF
   */
  getUserById: async (id: string): Promise<UserDetail> => {
    const response = await apiClient.get<UserDetail>(`/users/${id}`);
    return response.data;
  },

  /**
   * Create new user
   * @requires ROLE_ADMIN
   */
  createUser: async (data: CreateUserRequest): Promise<UserDetail> => {
    const response = await apiClient.post<UserDetail>("/users", data);
    return response.data;
  },

  /**
   * Update user
   * @requires ROLE_ADMIN
   */
  updateUser: async (
    id: string,
    data: UpdateUserRequest
  ): Promise<UserDetail> => {
    const response = await apiClient.put<UserDetail>(`/users/${id}`, data);
    return response.data;
  },

  /**
   * Delete user (soft delete)
   * @requires ROLE_ADMIN
   */
  deleteUser: async (id: string): Promise<void> => {
    await apiClient.delete(`/users/${id}`);
  },

  /**
   * Restore deleted user
   * @requires ROLE_ADMIN
   */
  restoreUser: async (id: string): Promise<UserDetail> => {
    const response = await apiClient.patch<UserDetail>(`/users/${id}/restore`);
    return response.data;
  },

  /**
   * Assign roles to user (replaces all existing roles)
   * @requires ROLE_ADMIN
   */
  assignRoles: async (userId: string, roleIds: string[]): Promise<UserDetail> => {
    const response = await apiClient.post<UserDetail>(
      `/users/${userId}/roles`,
      roleIds
    );
    return response.data;
  },

  /**
   * Remove roles from user
   * @requires ROLE_ADMIN
   */
  removeRoles: async (userId: string, roleIds: string[]): Promise<UserDetail> => {
    const response = await apiClient.delete<UserDetail>(
      `/users/${userId}/roles`,
      { data: roleIds }
    );
    return response.data;
  },

  /**
   * Change password
   * @requires ROLE_ADMIN or own user
   */
  changePassword: async (
    userId: string,
    data: ChangePasswordRequest
  ): Promise<void> => {
    await apiClient.post(`/users/${userId}/change-password`, data);
  },

  /**
   * Verify email
   * @requires ROLE_ADMIN
   */
  verifyEmail: async (userId: string): Promise<void> => {
    await apiClient.post(`/users/${userId}/verify-email`);
  },

  /**
   * Get all roles (for role assignment)
   * @requires ROLE_ADMIN
   */
  getAllRoles: async (): Promise<Role[]> => {
    const response = await apiClient.get<Role[]>("/roles");
    return response.data;
  },
};