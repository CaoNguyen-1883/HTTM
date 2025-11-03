import axios from 'axios';

// ML API Base URL
const ML_API_URL = import.meta.env.VITE_ML_API_URL || 'http://localhost:8000';

export interface RecommendationItem {
  product_id: string;
  score: number;
}

export interface RecommendationsResponse {
  recommendations: RecommendationItem[];
  total: number;
}

/**
 * Get personalized recommendations for a user
 * Combines purchase history + view history for better recommendations
 */
export const getUserRecommendations = async (
  userId: string,
  limit: number = 20,
  useViews: boolean = true
): Promise<RecommendationsResponse> => {
  try {
    const response = await axios.get<RecommendationsResponse>(
      `${ML_API_URL}/api/recommendations/user/${userId}`,
      {
        params: { limit, use_views: useViews },
        timeout: 15000, // Increased to 15 seconds
      }
    );
    return response.data;
  } catch (error) {
    console.error('Error fetching user recommendations:', error);
    // Return empty recommendations on error
    return { recommendations: [], total: 0 };
  }
};

/**
 * Get similar products (frequently bought together)
 * Use case: Product detail page - "Customers also bought"
 */
export const getSimilarProducts = async (
  productId: string,
  limit: number = 10
): Promise<RecommendationsResponse> => {
  try {
    const response = await axios.get<RecommendationsResponse>(
      `${ML_API_URL}/api/recommendations/similar/${productId}`,
      {
        params: { limit },
        timeout: 15000, // Increased to 15 seconds
      }
    );
    return response.data;
  } catch (error) {
    console.error('Error fetching similar products:', error);
    return { recommendations: [], total: 0 };
  }
};

/**
 * Get popular products (fallback for cold-start users)
 * Use case: Homepage, new users without history
 */
export const getPopularProducts = async (
  limit: number = 20
): Promise<any> => {
  try {
    const response = await axios.get(
      `${ML_API_URL}/api/recommendations/popular`,
      {
        params: { limit },
        timeout: 5000,
      }
    );
    return response.data;
  } catch (error) {
    console.error('Error fetching popular products:', error);
    return { recommendations: [], total: 0 };
  }
};

/**
 * Check ML API health
 */
export const checkMLApiHealth = async (): Promise<boolean> => {
  try {
    const response = await axios.get(`${ML_API_URL}/health`, {
      timeout: 2000,
    });
    return response.data.status === 'healthy';
  } catch (error) {
    return false;
  }
};
