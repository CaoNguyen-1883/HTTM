import { useQuery } from "@tanstack/react-query";
import { apiClient } from "../api/client";
import { ProductSummary } from "../types";

export interface RecommendationSection {
  sectionTitle: string;
  sectionDescription: string;
  type: string;
  products: ProductSummary[];
}

// Get all homepage recommendations
export const useHomePageRecommendations = () => {
  return useQuery<RecommendationSection[]>({
    queryKey: ["recommendations", "homepage"],
    queryFn: async () => {
      const response = await apiClient.get("/recommendations/homepage");
      return response.data;
    },
    staleTime: 5 * 60 * 1000, // Cache for 5 minutes
  });
};

// Get trending products
export const useTrendingProducts = (limit: number = 8) => {
  return useQuery<RecommendationSection>({
    queryKey: ["recommendations", "trending", limit],
    queryFn: async () => {
      const response = await apiClient.get(`/recommendations/trending?limit=${limit}`);
      return response.data;
    },
    staleTime: 5 * 60 * 1000,
  });
};

// Get best sellers
export const useBestSellers = (limit: number = 8) => {
  return useQuery<RecommendationSection>({
    queryKey: ["recommendations", "best-sellers", limit],
    queryFn: async () => {
      const response = await apiClient.get(`/recommendations/best-sellers?limit=${limit}`);
      return response.data;
    },
    staleTime: 5 * 60 * 1000,
  });
};

// Get top rated
export const useTopRated = (limit: number = 8) => {
  return useQuery<RecommendationSection>({
    queryKey: ["recommendations", "top-rated", limit],
    queryFn: async () => {
      const response = await apiClient.get(`/recommendations/top-rated?limit=${limit}`);
      return response.data;
    },
    staleTime: 5 * 60 * 1000,
  });
};

// Get new arrivals
export const useNewArrivals = (limit: number = 8) => {
  return useQuery<RecommendationSection>({
    queryKey: ["recommendations", "new-arrivals", limit],
    queryFn: async () => {
      const response = await apiClient.get(`/recommendations/new-arrivals?limit=${limit}`);
      return response.data;
    },
    staleTime: 5 * 60 * 1000,
  });
};

// Get similar products
export const useSimilarProducts = (productId: string, limit: number = 8) => {
  return useQuery<RecommendationSection>({
    queryKey: ["recommendations", "similar", productId, limit],
    queryFn: async () => {
      const response = await apiClient.get(`/recommendations/similar/${productId}?limit=${limit}`);
      return response.data;
    },
    enabled: !!productId,
    staleTime: 5 * 60 * 1000,
  });
};

// Get frequently bought together
export const useFrequentlyBoughtTogether = (productId: string, limit: number = 5) => {
  return useQuery<RecommendationSection>({
    queryKey: ["recommendations", "bought-together", productId, limit],
    queryFn: async () => {
      const response = await apiClient.get(`/recommendations/bought-together/${productId}?limit=${limit}`);
      return response.data;
    },
    enabled: !!productId,
    staleTime: 5 * 60 * 1000,
  });
};

// Get personalized recommendations (for logged in users)
export const useRecommendationsForYou = (limit: number = 8) => {
  return useQuery<RecommendationSection>({
    queryKey: ["recommendations", "for-you", limit],
    queryFn: async () => {
      const response = await apiClient.get(`/recommendations/for-you?limit=${limit}`);
      return response.data;
    },
    staleTime: 2 * 60 * 1000, // Shorter cache for personalized
  });
};

// ==================== ML-BASED RECOMMENDATIONS ====================
// These hooks use the ML API (Item-Based Collaborative Filtering)

import {
  getUserRecommendations,
  getSimilarProducts as getMLSimilarProducts,
  RecommendationsResponse,
} from "../api/recommendations.api";

/**
 * ML-Based: Get personalized recommendations using collaborative filtering
 * Combines purchase history + view history for better accuracy
 */
export const useMLRecommendationsForYou = (
  userId: string | undefined,
  limit: number = 20
) => {
  return useQuery<RecommendationsResponse>({
    queryKey: ["ml-recommendations", "user", userId, limit],
    queryFn: () => getUserRecommendations(userId!, limit, true),
    enabled: !!userId,
    staleTime: 5 * 60 * 1000,
    retry: 1,
  });
};

/**
 * ML-Based: Get similar products using Item-Based CF
 * "Customers who bought this also bought..."
 */
export const useMLSimilarProducts = (
  productId: string | undefined,
  limit: number = 10
) => {
  return useQuery<RecommendationsResponse>({
    queryKey: ["ml-recommendations", "similar", productId, limit],
    queryFn: () => getMLSimilarProducts(productId!, limit),
    enabled: !!productId,
    staleTime: 10 * 60 * 1000,
    retry: 1,
  });
};
