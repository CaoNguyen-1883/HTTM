import { apiClient } from "./client";
import {
  Review,
  PageResponse,
  CreateReviewRequest,
  UpdateReviewRequest,
  ProductRatingSummary,
  ReplyReviewRequest,
  RejectReviewRequest,
  ReviewStatus,
} from "../types";

export const reviewsApi = {
  // ===================================
  // PUBLIC ENDPOINTS
  // ===================================

  /**
   * Get all approved reviews for a product
   * GET /api/reviews/product/{productId}
   */
  getProductReviews: async (
    productId: string,
    params?: { page?: number; size?: number; sort?: string }
  ): Promise<PageResponse<Review>> => {
    const response = await apiClient.get<PageResponse<Review>>(
      `/reviews/product/${productId}`,
      { params }
    );
    return response.data;
  },

  /**
   * Filter product reviews by rating
   * GET /api/reviews/product/{productId}/rating/{rating}
   */
  getProductReviewsByRating: async (
    productId: string,
    rating: number,
    params?: { page?: number; size?: number; sort?: string }
  ): Promise<PageResponse<Review>> => {
    const response = await apiClient.get<PageResponse<Review>>(
      `/reviews/product/${productId}/rating/${rating}`,
      { params }
    );
    return response.data;
  },

  /**
   * Get reviews from verified purchases only
   * GET /api/reviews/product/{productId}/verified
   */
  getVerifiedPurchaseReviews: async (
    productId: string,
    params?: { page?: number; size?: number; sort?: string }
  ): Promise<PageResponse<Review>> => {
    const response = await apiClient.get<PageResponse<Review>>(
      `/reviews/product/${productId}/verified`,
      { params }
    );
    return response.data;
  },

  /**
   * Get reviews sorted by helpfulness
   * GET /api/reviews/product/{productId}/helpful
   */
  getMostHelpfulReviews: async (
    productId: string,
    params?: { page?: number; size?: number }
  ): Promise<PageResponse<Review>> => {
    const response = await apiClient.get<PageResponse<Review>>(
      `/reviews/product/${productId}/helpful`,
      { params }
    );
    return response.data;
  },

  /**
   * Get rating statistics for a product
   * GET /api/reviews/product/{productId}/summary
   */
  getProductRatingSummary: async (
    productId: string
  ): Promise<ProductRatingSummary> => {
    const response = await apiClient.get<ProductRatingSummary>(
      `/reviews/product/${productId}/summary`
    );
    return response.data;
  },

  /**
   * Get review details by ID
   * GET /api/reviews/{reviewId}
   */
  getReviewById: async (reviewId: string): Promise<Review> => {
    const response = await apiClient.get<Review>(`/reviews/${reviewId}`);
    return response.data;
  },

  // ===================================
  // CUSTOMER ENDPOINTS
  // ===================================

  /**
   * Create a new product review
   * POST /api/reviews
   * @requires ROLE_CUSTOMER
   */
  createReview: async (data: CreateReviewRequest): Promise<Review> => {
    const response = await apiClient.post<Review>("/reviews", data);
    return response.data;
  },

  /**
   * Update your own review
   * PUT /api/reviews/{reviewId}
   * @requires ROLE_CUSTOMER
   */
  updateReview: async (
    reviewId: string,
    data: UpdateReviewRequest
  ): Promise<Review> => {
    const response = await apiClient.put<Review>(`/reviews/${reviewId}`, data);
    return response.data;
  },

  /**
   * Delete your own review
   * DELETE /api/reviews/{reviewId}
   * @requires ROLE_CUSTOMER
   */
  deleteReview: async (reviewId: string): Promise<void> => {
    await apiClient.delete(`/reviews/${reviewId}`);
  },

  /**
   * Get all reviews written by current user
   * GET /api/reviews/my-reviews
   * @requires ROLE_CUSTOMER
   */
  getMyReviews: async (params?: {
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<PageResponse<Review>> => {
    const response = await apiClient.get<PageResponse<Review>>(
      "/reviews/my-reviews",
      { params }
    );
    return response.data;
  },

  /**
   * Vote a review as helpful or not helpful
   * POST /api/reviews/{reviewId}/vote?isHelpful=true/false
   * @requires ROLE_CUSTOMER
   */
  voteReview: async (reviewId: string, isHelpful: boolean): Promise<Review> => {
    const response = await apiClient.post<Review>(
      `/reviews/${reviewId}/vote`,
      null,
      { params: { isHelpful } }
    );
    return response.data;
  },

  /**
   * Remove your vote from a review
   * DELETE /api/reviews/{reviewId}/vote
   * @requires ROLE_CUSTOMER
   */
  removeVote: async (reviewId: string): Promise<Review> => {
    const response = await apiClient.delete<Review>(
      `/reviews/${reviewId}/vote`
    );
    return response.data;
  },

  // ===================================
  // ADMIN/STAFF ENDPOINTS
  // ===================================

  /**
   * Get reviews filtered by status
   * GET /api/reviews/status/{status}
   * @requires ROLE_STAFF or ROLE_ADMIN
   */
  getReviewsByStatus: async (
    status: ReviewStatus,
    params?: { page?: number; size?: number; sort?: string }
  ): Promise<PageResponse<Review>> => {
    const response = await apiClient.get<PageResponse<Review>>(
      `/reviews/status/${status}`,
      { params }
    );
    return response.data;
  },

  /**
   * Get all pending reviews waiting for moderation
   * GET /api/reviews/pending
   * @requires ROLE_STAFF or ROLE_ADMIN
   */
  getPendingReviews: async (params?: {
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<PageResponse<Review>> => {
    const response = await apiClient.get<PageResponse<Review>>(
      "/reviews/pending",
      { params }
    );
    return response.data;
  },

  /**
   * Approve a pending review
   * POST /api/reviews/{reviewId}/approve
   * @requires ROLE_STAFF or ROLE_ADMIN
   */
  approveReview: async (reviewId: string): Promise<Review> => {
    const response = await apiClient.post<Review>(
      `/reviews/${reviewId}/approve`
    );
    return response.data;
  },

  /**
   * Reject a review
   * POST /api/reviews/{reviewId}/reject
   * @requires ROLE_STAFF or ROLE_ADMIN
   */
  rejectReview: async (
    reviewId: string,
    data: RejectReviewRequest
  ): Promise<Review> => {
    const response = await apiClient.post<Review>(
      `/reviews/${reviewId}/reject`,
      data
    );
    return response.data;
  },

  /**
   * Reply to a review
   * POST /api/reviews/{reviewId}/reply
   * @requires ROLE_SELLER, ROLE_STAFF, or ROLE_ADMIN
   */
  replyToReview: async (
    reviewId: string,
    data: ReplyReviewRequest
  ): Promise<Review> => {
    const response = await apiClient.post<Review>(
      `/reviews/${reviewId}/reply`,
      data
    );
    return response.data;
  },
};
