import { apiClient } from "./client";
import { Permission } from "../types";

export const permissionsApi = {
  /**
   * Get all permissions
   * @requires ROLE_ADMIN
   */
  getAllPermissions: async (): Promise<Permission[]> => {
    const response = await apiClient.get<Permission[]>("/permissions");
    return response.data;
  },

  /**
   * Get permission by ID
   * @requires ROLE_ADMIN
   */
  getPermissionById: async (id: string): Promise<Permission> => {
    const response = await apiClient.get<Permission>(`/permissions/${id}`);
    return response.data;
  },

  /**
   * Create new permission
   * @requires ROLE_ADMIN
   */
  createPermission: async (data: {
    name: string;
    description?: string;
  }): Promise<Permission> => {
    const response = await apiClient.post<Permission>("/permissions", data);
    return response.data;
  },

  /**
   * Update permission
   * @requires ROLE_ADMIN
   */
  updatePermission: async (
    id: string,
    data: {
      name?: string;
      description?: string;
    }
  ): Promise<Permission> => {
    const response = await apiClient.put<Permission>(`/permissions/${id}`, data);
    return response.data;
  },

  /**
   * Delete permission (soft delete)
   * @requires ROLE_ADMIN
   */
  deletePermission: async (id: string): Promise<void> => {
    await apiClient.delete(`/permissions/${id}`);
  },

  /**
   * Restore deleted permission
   * @requires ROLE_ADMIN
   */
  restorePermission: async (id: string): Promise<Permission> => {
    const response = await apiClient.patch<Permission>(`/permissions/${id}/restore`);
    return response.data;
  },
};