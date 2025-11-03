import { useQuery, useMutation, useQueryClient, UseQueryOptions } from "@tanstack/react-query";
import { reviewsApi } from "../api";
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

// ===================================
// PUBLIC HOOKS
// ===================================

/**
 * Get all approved reviews for a product
 */
export const useProductReviews = (
  productId: string,
  params?: { page?: number; size?: number; sort?: string },
  options?: UseQueryOptions<PageResponse<Review>>
) => {
  return useQuery({
    queryKey: ["reviews", "product", productId, params],
    queryFn: () => reviewsApi.getProductReviews(productId, params),
    enabled: !!productId,
    ...options,
  });
};

/**
 * Get product reviews filtered by rating
 */
export const useProductReviewsByRating = (
  productId: string,
  rating: number,
  params?: { page?: number; size?: number; sort?: string },
  options?: UseQueryOptions<PageResponse<Review>>
) => {
  return useQuery({
    queryKey: ["reviews", "product", productId, "rating", rating, params],
    queryFn: () => reviewsApi.getProductReviewsByRating(productId, rating, params),
    enabled: !!productId && rating > 0,
    ...options,
  });
};

/**
 * Get verified purchase reviews
 */
export const useVerifiedPurchaseReviews = (
  productId: string,
  params?: { page?: number; size?: number; sort?: string },
  options?: UseQueryOptions<PageResponse<Review>>
) => {
  return useQuery({
    queryKey: ["reviews", "product", productId, "verified", params],
    queryFn: () => reviewsApi.getVerifiedPurchaseReviews(productId, params),
    enabled: !!productId,
    ...options,
  });
};

/**
 * Get most helpful reviews
 */
export const useMostHelpfulReviews = (
  productId: string,
  params?: { page?: number; size?: number },
  options?: UseQueryOptions<PageResponse<Review>>
) => {
  return useQuery({
    queryKey: ["reviews", "product", productId, "helpful", params],
    queryFn: () => reviewsApi.getMostHelpfulReviews(productId, params),
    enabled: !!productId,
    ...options,
  });
};

/**
 * Get product rating summary
 */
export const useProductRatingSummary = (
  productId: string,
  options?: UseQueryOptions<ProductRatingSummary>
) => {
  return useQuery({
    queryKey: ["reviews", "product", productId, "summary"],
    queryFn: () => reviewsApi.getProductRatingSummary(productId),
    enabled: !!productId,
    ...options,
  });
};

/**
 * Get review by ID
 */
export const useReview = (
  reviewId: string,
  options?: UseQueryOptions<Review>
) => {
  return useQuery({
    queryKey: ["reviews", reviewId],
    queryFn: () => reviewsApi.getReviewById(reviewId),
    enabled: !!reviewId,
    ...options,
  });
};

// ===================================
// CUSTOMER HOOKS
// ===================================

/**
 * Create review mutation
 */
export const useCreateReview = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateReviewRequest) => reviewsApi.createReview(data),
    onSuccess: (data) => {
      // Invalidate product reviews
      queryClient.invalidateQueries({ queryKey: ["reviews", "product", data.productId] });
      // Invalidate my reviews
      queryClient.invalidateQueries({ queryKey: ["reviews", "my-reviews"] });
    },
  });
};

/**
 * Update review mutation
 */
export const useUpdateReview = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ reviewId, data }: { reviewId: string; data: UpdateReviewRequest }) =>
      reviewsApi.updateReview(reviewId, data),
    onSuccess: (data) => {
      // Invalidate product reviews
      queryClient.invalidateQueries({ queryKey: ["reviews", "product", data.productId] });
      // Invalidate specific review
      queryClient.invalidateQueries({ queryKey: ["reviews", data.id] });
      // Invalidate my reviews
      queryClient.invalidateQueries({ queryKey: ["reviews", "my-reviews"] });
    },
  });
};

/**
 * Delete review mutation
 */
export const useDeleteReview = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (reviewId: string) => reviewsApi.deleteReview(reviewId),
    onSuccess: () => {
      // Invalidate all reviews queries
      queryClient.invalidateQueries({ queryKey: ["reviews"] });
    },
  });
};

/**
 * Get my reviews
 */
export const useMyReviews = (
  params?: { page?: number; size?: number; sort?: string },
  options?: UseQueryOptions<PageResponse<Review>>
) => {
  return useQuery({
    queryKey: ["reviews", "my-reviews", params],
    queryFn: () => reviewsApi.getMyReviews(params),
    ...options,
  });
};

/**
 * Vote review mutation
 */
export const useVoteReview = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ reviewId, isHelpful }: { reviewId: string; isHelpful: boolean }) =>
      reviewsApi.voteReview(reviewId, isHelpful),
    onSuccess: (data) => {
      // Update the specific review in cache
      queryClient.setQueryData(["reviews", data.id], data);
      // Invalidate product reviews to refresh the list
      queryClient.invalidateQueries({ queryKey: ["reviews", "product", data.productId] });
    },
  });
};

/**
 * Remove vote mutation
 */
export const useRemoveVote = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (reviewId: string) => reviewsApi.removeVote(reviewId),
    onSuccess: (data) => {
      // Update the specific review in cache
      queryClient.setQueryData(["reviews", data.id], data);
      // Invalidate product reviews to refresh the list
      queryClient.invalidateQueries({ queryKey: ["reviews", "product", data.productId] });
    },
  });
};

// ===================================
// ADMIN/STAFF HOOKS
// ===================================

/**
 * Get reviews by status
 */
export const useReviewsByStatus = (
  status: ReviewStatus,
  params?: { page?: number; size?: number; sort?: string },
  options?: UseQueryOptions<PageResponse<Review>>
) => {
  return useQuery({
    queryKey: ["reviews", "status", status, params],
    queryFn: () => reviewsApi.getReviewsByStatus(status, params),
    ...options,
  });
};

/**
 * Get pending reviews
 */
export const usePendingReviews = (
  params?: { page?: number; size?: number; sort?: string },
  options?: UseQueryOptions<PageResponse<Review>>
) => {
  return useQuery({
    queryKey: ["reviews", "pending", params],
    queryFn: () => reviewsApi.getPendingReviews(params),
    ...options,
  });
};

/**
 * Approve review mutation
 */
export const useApproveReview = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (reviewId: string) => reviewsApi.approveReview(reviewId),
    onSuccess: () => {
      // Invalidate pending and status queries
      queryClient.invalidateQueries({ queryKey: ["reviews", "pending"] });
      queryClient.invalidateQueries({ queryKey: ["reviews", "status"] });
    },
  });
};

/**
 * Reject review mutation
 */
export const useRejectReview = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ reviewId, data }: { reviewId: string; data: RejectReviewRequest }) =>
      reviewsApi.rejectReview(reviewId, data),
    onSuccess: () => {
      // Invalidate pending and status queries
      queryClient.invalidateQueries({ queryKey: ["reviews", "pending"] });
      queryClient.invalidateQueries({ queryKey: ["reviews", "status"] });
    },
  });
};

/**
 * Reply to review mutation
 */
export const useReplyToReview = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ reviewId, data }: { reviewId: string; data: ReplyReviewRequest }) =>
      reviewsApi.replyToReview(reviewId, data),
    onSuccess: (data) => {
      // Update the specific review in cache
      queryClient.setQueryData(["reviews", data.id], data);
      // Invalidate product reviews to refresh the list
      queryClient.invalidateQueries({ queryKey: ["reviews", "product", data.productId] });
    },
  });
};
