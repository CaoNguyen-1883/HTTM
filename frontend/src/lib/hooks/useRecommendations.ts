import { useQuery } from "@tanstack/react-query";
import { apiClient } from "../api/client";
import { ProductSummary } from "../types";

export interface RecommendationSection {
  sectionTitle: string;
  sectionDescription: string;
  type: string;
  products: ProductSummary[];
}

// Get all homepage recommendations (Trending, Best Sellers, New Arrivals, Top Rated)
// NOTE: This does NOT include "For You" section - use useRecommendationsForYou for that
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
      const response = await apiClient.get(
        `/recommendations/trending?limit=${limit}`,
      );
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
      const response = await apiClient.get(
        `/recommendations/best-sellers?limit=${limit}`,
      );
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
      const response = await apiClient.get(
        `/recommendations/top-rated?limit=${limit}`,
      );
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
      const response = await apiClient.get(
        `/recommendations/new-arrivals?limit=${limit}`,
      );
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
      const response = await apiClient.get(
        `/recommendations/similar/${productId}?limit=${limit}`,
      );
      return response.data;
    },
    enabled: !!productId,
    staleTime: 5 * 60 * 1000,
  });
};

// Get frequently bought together
export const useFrequentlyBoughtTogether = (
  productId: string,
  limit: number = 5,
) => {
  return useQuery<RecommendationSection>({
    queryKey: ["recommendations", "bought-together", productId, limit],
    queryFn: async () => {
      const response = await apiClient.get(
        `/recommendations/bought-together/${productId}?limit=${limit}`,
      );
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
      const response = await apiClient.get(
        `/recommendations/for-you?limit=${limit}`,
      );
      return response.data;
    },
    staleTime: 2 * 60 * 1000, // Shorter cache for personalized
  });
};

// ==================== ML-BASED RECOMMENDATIONS ====================
// These hooks use the ML API (Item-Based Collaborative Filtering)

import {
  getSimilarProducts as getMLSimilarProducts,
  RecommendationsResponse,
} from "../api/recommendations.api";

/**
 * ML-Based: Get personalized recommendations using collaborative filtering
 * NOTE: This returns only product IDs. Use usePersonalizedProducts for full details.
 * @deprecated Use usePersonalizedProducts instead for automatic enrichment
 */
export const useMLRecommendationsForYou = (
  userId: string | undefined,
  limit: number = 20,
) => {
  return useQuery<RecommendationsResponse>({
    queryKey: ["ml-recommendations", "user", userId, limit],
    queryFn: async () => {
      // This hook is kept for backward compatibility but needs purchase history
      // For new code, use usePersonalizedProducts instead
      return { recommendations: [], total: 0 };
    },
    enabled: false, // Disabled - use usePersonalizedProducts instead
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
  limit: number = 10,
) => {
  return useQuery<RecommendationsResponse>({
    queryKey: ["ml-recommendations", "similar", productId, limit],
    queryFn: () => getMLSimilarProducts(productId!, limit),
    enabled: !!productId,
    staleTime: 10 * 60 * 1000,
    retry: 1,
  });
};

// ==================== ENRICHED ML RECOMMENDATIONS ====================
// These hooks return full product details (enriched with Spring Boot data)

import { enrichedRecommendationsApi } from "../api/recommendations.helper";

/**
 * Get personalized recommendations với FULL product details
 * Backend handles user authentication and ML API calls automatically
 * Use case: Homepage "Dành Cho Bạn" section
 */
export const usePersonalizedProducts = (limit: number = 20) => {
  return useQuery({
    queryKey: ["personalizedProducts", limit],
    queryFn: async () => {
      // Call /recommendations/for-you - backend handles everything:
      // - Authenticated users: ML API based on cart/purchase history
      // - Guest users: Trending products
      const response = await apiClient.get(
        `/recommendations/for-you?limit=${limit}`,
      );
      return response.data;
    },
    enabled: true,
    staleTime: 2 * 60 * 1000, // Cache 2 minutes
    retry: 1,
  });
};

/**
 * Get similar products với FULL product details
 * Use case: Product Detail Page "Khách Hàng Cũng Mua"
 */
export const useSimilarProductsEnriched = (
  productId: string,
  limit: number = 10,
) => {
  return useQuery({
    queryKey: ["similarProductsEnriched", productId, limit],
    queryFn: () =>
      enrichedRecommendationsApi.getSimilarProductsWithDetails(
        productId,
        limit,
      ),
    enabled: !!productId,
    staleTime: 10 * 60 * 1000, // Cache 10 minutes
    retry: 1,
  });
};

/**
 * Get popular products với FULL product details
 * Use case: Homepage "Best Sellers", Guest Users
 */
export const usePopularProductsEnriched = (limit: number = 20) => {
  return useQuery({
    queryKey: ["popularProductsEnriched", limit],
    queryFn: () =>
      enrichedRecommendationsApi.getPopularProductsWithDetails(limit),
    staleTime: 30 * 60 * 1000, // Cache 30 minutes
    retry: 2,
  });
};
