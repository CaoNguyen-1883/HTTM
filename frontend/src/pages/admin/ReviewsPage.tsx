import { useState } from "react";
import { ReviewStatus } from "../../lib/types";
import { Filter, ChevronLeft, ChevronRight, CheckCircle, XCircle, Star, MessageSquare, X } from "lucide-react";
import { usePendingReviews, useReviewsByStatus, useApproveReview, useRejectReview, useReplyToReview } from "../../lib/hooks/useReviews";

export const AdminReviewsPage = () => {
  const [currentPage, setCurrentPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<ReviewStatus | "">("");
  const [ratingFilter, setRatingFilter] = useState<number | "">("");

  // Modal states
  const [rejectModalOpen, setRejectModalOpen] = useState(false);
  const [replyModalOpen, setReplyModalOpen] = useState(false);
  const [selectedReviewId, setSelectedReviewId] = useState<string>("");
  const [rejectReason, setRejectReason] = useState("");
  const [replyText, setReplyText] = useState("");

  // Fetch reviews based on filter
  const queryParams = { page: currentPage, size: 20 };

  // Determine which query to use based on filter
  const shouldFetchByStatus = !!statusFilter && statusFilter !== ReviewStatus.PENDING;

  const { data: pendingData, isLoading: pendingLoading } = usePendingReviews(
    queryParams
  );

  const { data: statusData, isLoading: statusLoading } = useReviewsByStatus(
    statusFilter as ReviewStatus,
    queryParams
  );

  // Determine which data to use
  const reviewsData = shouldFetchByStatus ? statusData : pendingData;
  const isLoading = shouldFetchByStatus ? statusLoading : pendingLoading;

  // Mutations
  const approveReview = useApproveReview();
  const rejectReview = useRejectReview();
  const replyToReview = useReplyToReview();

  const handlePageChange = (newPage: number) => {
    setCurrentPage(newPage);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleApprove = async (reviewId: string) => {
    try {
      await approveReview.mutateAsync(reviewId);
      alert("Review approved successfully");
    } catch (error: any) {
      alert(error.response?.data?.message || "Failed to approve review");
    }
  };

  const handleRejectClick = (reviewId: string) => {
    setSelectedReviewId(reviewId);
    setRejectReason("");
    setRejectModalOpen(true);
  };

  const handleRejectSubmit = async () => {
    if (!rejectReason.trim()) {
      alert("Please provide a reason for rejection");
      return;
    }

    try {
      await rejectReview.mutateAsync({
        reviewId: selectedReviewId,
        data: { reason: rejectReason },
      });
      alert("Review rejected successfully");
      setRejectModalOpen(false);
      setRejectReason("");
    } catch (error: any) {
      alert(error.response?.data?.message || "Failed to reject review");
    }
  };

  const handleReplyClick = (reviewId: string) => {
    setSelectedReviewId(reviewId);
    setReplyText("");
    setReplyModalOpen(true);
  };

  const handleReplySubmit = async () => {
    if (!replyText.trim()) {
      alert("Please enter a reply");
      return;
    }

    try {
      await replyToReview.mutateAsync({
        reviewId: selectedReviewId,
        data: { content: replyText },
      });
      alert("Reply added successfully");
      setReplyModalOpen(false);
      setReplyText("");
    } catch (error: any) {
      alert(error.response?.data?.message || "Failed to add reply");
    }
  };

  const getStatusBadge = (status: ReviewStatus) => {
    const styles = {
      [ReviewStatus.PENDING]: "bg-yellow-100 text-yellow-700",
      [ReviewStatus.APPROVED]: "bg-green-100 text-green-700",
      [ReviewStatus.REJECTED]: "bg-red-100 text-red-700",
    };

    return (
      <span className={`px-2 py-1 text-xs font-medium rounded-full ${styles[status]}`}>
        {status}
      </span>
    );
  };

  const renderStars = (rating: number) => {
    return (
      <div className="flex gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <Star
            key={star}
            className={`w-4 h-4 ${
              star <= rating ? "text-yellow-400 fill-yellow-400" : "text-gray-300"
            }`}
          />
        ))}
      </div>
    );
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Reviews Management</h1>
        <p className="text-gray-600 mt-1">
          Moderate and manage customer reviews
        </p>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-lg shadow p-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Status Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              <Filter className="inline w-4 h-4 mr-1" />
              Review Status
            </label>
            <select
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value as ReviewStatus | "");
                setCurrentPage(0);
              }}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="">All Statuses</option>
              {Object.values(ReviewStatus).map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
          </div>

          {/* Rating Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              <Filter className="inline w-4 h-4 mr-1" />
              Rating
            </label>
            <select
              value={ratingFilter}
              onChange={(e) => {
                setRatingFilter(e.target.value ? Number(e.target.value) : "");
                setCurrentPage(0);
              }}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="">All Ratings</option>
              {[5, 4, 3, 2, 1].map((rating) => (
                <option key={rating} value={rating}>
                  {rating} Stars
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Reviews List */}
      {isLoading ? (
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Loading reviews...</div>
        </div>
      ) : !reviewsData || reviewsData.content.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-12 text-center">
          <div className="text-gray-500 text-lg">No reviews found</div>
          <p className="text-sm text-gray-400 mt-2">
            Reviews will appear here when customers submit them
          </p>
          {(statusFilter || ratingFilter) && (
            <button
              onClick={() => {
                setStatusFilter("");
                setRatingFilter("");
              }}
              className="mt-4 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              Clear Filters
            </button>
          )}
        </div>
      ) : (
        <>
          <div className="space-y-4">
            {reviewsData?.content.map((review: any) => (
              <div
                key={review.id}
                className="bg-white rounded-lg shadow p-6 hover:shadow-md transition-shadow"
              >
                <div className="flex items-start justify-between mb-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <img
                        src={review.userAvatarUrl || "https://via.placeholder.com/40"}
                        alt={review.userName}
                        className="w-10 h-10 rounded-full"
                      />
                      <div>
                        <h4 className="font-semibold text-gray-900">{review.userName}</h4>
                        <div className="flex items-center gap-2">
                          {renderStars(review.rating)}
                          {review.isVerifiedPurchase && (
                            <span className="text-xs text-green-600 font-medium">
                              ✓ Verified Purchase
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                    <h5 className="font-medium text-gray-900 mb-1">{review.title}</h5>
                    <p className="text-gray-600 text-sm mb-2">{review.comment}</p>
                    <div className="text-xs text-gray-500">
                      Product: <span className="font-medium">{review.productName}</span>
                      <span className="mx-2">•</span>
                      {new Date(review.createdAt).toLocaleDateString()}
                    </div>
                  </div>
                  <div className="ml-4">
                    {getStatusBadge(review.status)}
                  </div>
                </div>

                {/* Action buttons for pending reviews */}
                {review.status === ReviewStatus.PENDING && (
                  <div className="flex gap-2 pt-4 border-t">
                    <button
                      onClick={() => handleApprove(review.id)}
                      disabled={approveReview.isPending}
                      className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <CheckCircle className="w-4 h-4" />
                      {approveReview.isPending ? "Approving..." : "Approve"}
                    </button>
                    <button
                      onClick={() => handleRejectClick(review.id)}
                      disabled={rejectReview.isPending}
                      className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <XCircle className="w-4 h-4" />
                      Reject
                    </button>
                  </div>
                )}

                {/* Reply button for approved reviews */}
                {review.status === ReviewStatus.APPROVED && !review.sellerReply && (
                  <div className="flex gap-2 pt-4 border-t">
                    <button
                      onClick={() => handleReplyClick(review.id)}
                      className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm font-medium"
                    >
                      <MessageSquare className="w-4 h-4" />
                      Reply to Review
                    </button>
                  </div>
                )}

                {review.sellerReply && (
                  <div className="mt-4 p-4 bg-gray-50 rounded-lg border-l-4 border-blue-500">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-sm font-medium text-gray-900">Seller Reply:</span>
                      <span className="text-xs text-gray-500">
                        {new Date(review.sellerRepliedAt).toLocaleDateString()}
                      </span>
                    </div>
                    <p className="text-sm text-gray-600">{review.sellerReply}</p>
                  </div>
                )}
              </div>
            ))}
          </div>

          {/* Pagination */}
          {reviewsData && reviewsData.totalPages > 1 && (
            <div className="flex items-center justify-between bg-white rounded-lg shadow px-6 py-4">
              <div className="text-sm text-gray-600">
                Showing page {currentPage + 1} of {reviewsData.totalPages} (
                {reviewsData.totalElements} total reviews)
              </div>
              <div className="flex gap-2">
                <button
                  onClick={() => handlePageChange(currentPage - 1)}
                  disabled={currentPage === 0}
                  className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                >
                  <ChevronLeft className="w-4 h-4" />
                  Previous
                </button>
                <button
                  onClick={() => handlePageChange(currentPage + 1)}
                  disabled={currentPage >= reviewsData.totalPages - 1}
                  className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                >
                  Next
                  <ChevronRight className="w-4 h-4" />
                </button>
              </div>
            </div>
          )}
        </>
      )}

      {/* Reject Modal */}
      {rejectModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full mx-4">
            <div className="flex items-center justify-between p-6 border-b">
              <h3 className="text-xl font-semibold text-gray-900">Reject Review</h3>
              <button
                onClick={() => setRejectModalOpen(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="w-6 h-6" />
              </button>
            </div>
            <div className="p-6">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Reason for Rejection *
              </label>
              <textarea
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
                placeholder="Please provide a reason for rejecting this review..."
                rows={4}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent"
              />
            </div>
            <div className="flex gap-3 px-6 py-4 bg-gray-50 rounded-b-lg">
              <button
                onClick={() => setRejectModalOpen(false)}
                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-100 font-medium"
              >
                Cancel
              </button>
              <button
                onClick={handleRejectSubmit}
                disabled={rejectReview.isPending || !rejectReason.trim()}
                className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 font-medium disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {rejectReview.isPending ? "Rejecting..." : "Reject Review"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Reply Modal */}
      {replyModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full mx-4">
            <div className="flex items-center justify-between p-6 border-b">
              <h3 className="text-xl font-semibold text-gray-900">Reply to Review</h3>
              <button
                onClick={() => setReplyModalOpen(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="w-6 h-6" />
              </button>
            </div>
            <div className="p-6">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Your Reply *
              </label>
              <textarea
                value={replyText}
                onChange={(e) => setReplyText(e.target.value)}
                placeholder="Write your reply to the customer..."
                rows={4}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div className="flex gap-3 px-6 py-4 bg-gray-50 rounded-b-lg">
              <button
                onClick={() => setReplyModalOpen(false)}
                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-100 font-medium"
              >
                Cancel
              </button>
              <button
                onClick={handleReplySubmit}
                disabled={replyToReview.isPending || !replyText.trim()}
                className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {replyToReview.isPending ? "Sending..." : "Send Reply"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
