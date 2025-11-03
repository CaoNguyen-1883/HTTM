import { apiClient } from "./client";
import { Role } from "../types";

export const rolesApi = {
  /**
   * Get all roles
   * @requires ROLE_ADMIN
   */
  getAllRoles: async (): Promise<Role[]> => {
    const response = await apiClient.get<Role[]>("/roles");
    return response.data;
  },

  /**
   * Get role by ID
   * @requires ROLE_ADMIN
   */
  getRoleById: async (id: string): Promise<Role> => {
    const response = await apiClient.get<Role>(`/roles/${id}`);
    return response.data;
  },

  /**
   * Create new role
   * @requires ROLE_ADMIN
   */
  createRole: async (data: {
    name: string;
    description?: string;
    permissionIds?: string[];
  }): Promise<Role> => {
    const response = await apiClient.post<Role>("/roles", data);
    return response.data;
  },

  /**
   * Update role
   * @requires ROLE_ADMIN
   */
  updateRole: async (
    id: string,
    data: {
      name?: string;
      description?: string;
      permissionIds?: string[];
    }
  ): Promise<Role> => {
    const response = await apiClient.put<Role>(`/roles/${id}`, data);
    return response.data;
  },

  /**
   * Delete role (soft delete)
   * @requires ROLE_ADMIN
   */
  deleteRole: async (id: string): Promise<void> => {
    await apiClient.delete(`/roles/${id}`);
  },

  /**
   * Restore deleted role
   * @requires ROLE_ADMIN
   */
  restoreRole: async (id: string): Promise<Role> => {
    const response = await apiClient.patch<Role>(`/roles/${id}/restore`);
    return response.data;
  },

  /**
   * Assign permissions to role
   * @requires ROLE_ADMIN
   */
  assignPermissions: async (roleId: string, permissionIds: string[]): Promise<Role> => {
    const response = await apiClient.post<Role>(
      `/roles/${roleId}/permissions`,
      permissionIds
    );
    return response.data;
  },

  /**
   * Remove permissions from role
   * @requires ROLE_ADMIN
   */
  removePermissions: async (roleId: string, permissionIds: string[]): Promise<Role> => {
    const response = await apiClient.delete<Role>(
      `/roles/${roleId}/permissions`,
      { data: permissionIds }
    );
    return response.data;
  },
};