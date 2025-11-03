import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { cacheApi } from "../api/cache.api";

/**
 * Hook to get all cache names
 */
export const useCacheNames = () => {
  return useQuery({
    queryKey: ["cacheNames"],
    queryFn: cacheApi.getCacheNames,
  });
};

/**
 * Hook to clear all caches
 */
export const useClearAllCaches = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: cacheApi.clearAll,
    onSuccess: () => {
      // Invalidate all queries to refetch fresh data
      queryClient.invalidateQueries();
    },
  });
};

/**
 * Hook to clear a specific cache
 */
export const useClearCache = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (cacheName: string) => cacheApi.clearCache(cacheName),
    onSuccess: () => {
      // Invalidate all queries to refetch fresh data
      queryClient.invalidateQueries();
    },
  });
};
