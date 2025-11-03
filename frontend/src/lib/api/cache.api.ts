import { apiClient } from "./client";

/**
 * Cache Management API
 * Admin-only endpoints for managing Redis cache
 */

export const cacheApi = {
  /**
   * Clear all caches
   */
  clearAll: async (): Promise<string> => {
    const response = await apiClient.post<string>("/admin/cache/clear-all");
    return response.data;
  },

  /**
   * Clear a specific cache by name
   */
  clearCache: async (cacheName: string): Promise<string> => {
    const response = await apiClient.post<string>(`/admin/cache/clear/${cacheName}`);
    return response.data;
  },

  /**
   * Get all cache names
   */
  getCacheNames: async (): Promise<string[]> => {
    const response = await apiClient.get<string[]>("/admin/cache/names");
    return response.data;
  },
};
