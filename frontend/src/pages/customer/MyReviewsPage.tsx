import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { MessageSquare, Edit, Trash2, Package } from "lucide-react";
import { useMyReviews, useDeleteReview, useUpdateReview } from "../../lib/hooks";
import { ReviewForm } from "../../components/shared/ReviewForm";
import { Review, UpdateReviewRequest } from "../../lib/types";

export const MyReviewsPage = () => {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [editingReview, setEditingReview] = useState<Review | null>(null);

  const { data: reviewsData, isLoading } = useMyReviews({ page, size: 10 });
  const deleteReviewMutation = useDeleteReview();
  const updateReviewMutation = useUpdateReview();

  const handleDelete = async (reviewId: string) => {
    if (!confirm("Are you sure you want to delete this review?")) {
      return;
    }

    try {
      await deleteReviewMutation.mutateAsync(reviewId);
      alert("Review deleted successfully");
    } catch (error: any) {
      alert(error.response?.data?.message || "Failed to delete review");
    }
  };

  const handleUpdate = async (data: UpdateReviewRequest) => {
    if (!editingReview) return;

    try {
      await updateReviewMutation.mutateAsync({
        reviewId: editingReview.id,
        data,
      });
      setEditingReview(null);
      alert("Review updated successfully");
    } catch (error: any) {
      alert(error.response?.data?.message || "Failed to update review");
    }
  };

  if (isLoading && !reviewsData) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500">Loading your reviews...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-3">
            <MessageSquare className="w-8 h-8" />
            My Reviews
          </h1>
          <p className="text-gray-600 mt-1">
            Manage and edit your product reviews
          </p>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Editing Form */}
        {editingReview && (
          <div className="mb-8">
            <ReviewForm
              productId={editingReview.productId}
              initialData={{
                rating: editingReview.rating,
                title: editingReview.title,
                content: editingReview.content,
                imageUrls: editingReview.images.map((img) => img.imageUrl),
              }}
              onSubmit={handleUpdate}
              onCancel={() => setEditingReview(null)}
              isSubmitting={updateReviewMutation.isPending}
              mode="edit"
            />
          </div>
        )}

        {/* Reviews List */}
        {reviewsData && reviewsData.content.length > 0 ? (
          <div>
            {/* Custom Review List with Edit/Delete actions */}
            <div className="bg-white rounded-lg border divide-y">
              {reviewsData.content.map((review) => (
                <div key={review.id} className="p-6">
                  {/* Product Info */}
                  <div className="flex items-start justify-between mb-4 pb-4 border-b">
                    <div className="flex items-start gap-3">
                      <Package className="w-5 h-5 text-gray-400 mt-1" />
                      <div>
                        <button
                          onClick={() => navigate(`/products/${review.productId}`)}
                          className="font-semibold text-gray-900 hover:text-blue-600 transition"
                        >
                          {review.productName}
                        </button>
                        <div className="flex items-center gap-2 mt-1">
                          <span className={`text-xs px-2 py-1 rounded ${
                            review.status === "APPROVED"
                              ? "bg-green-100 text-green-700"
                              : review.status === "PENDING"
                              ? "bg-yellow-100 text-yellow-700"
                              : "bg-red-100 text-red-700"
                          }`}>
                            {review.status}
                          </span>
                        </div>
                      </div>
                    </div>

                    {/* Actions */}
                    <div className="flex gap-2">
                      <button
                        onClick={() => setEditingReview(review)}
                        disabled={review.status === "REJECTED"}
                        className="p-2 text-blue-600 hover:bg-blue-50 rounded transition disabled:opacity-50 disabled:cursor-not-allowed"
                        title="Edit review"
                      >
                        <Edit className="w-5 h-5" />
                      </button>
                      <button
                        onClick={() => handleDelete(review.id)}
                        disabled={deleteReviewMutation.isPending}
                        className="p-2 text-red-600 hover:bg-red-50 rounded transition disabled:opacity-50"
                        title="Delete review"
                      >
                        <Trash2 className="w-5 h-5" />
                      </button>
                    </div>
                  </div>

                  {/* Review Content */}
                  <div>
                    <div className="flex items-center gap-2 mb-2">
                      <div className="flex">
                        {[1, 2, 3, 4, 5].map((star) => (
                          <svg
                            key={star}
                            className={`w-5 h-5 ${
                              star <= review.rating
                                ? "fill-yellow-400 text-yellow-400"
                                : "text-gray-300"
                            }`}
                            fill="currentColor"
                            viewBox="0 0 20 20"
                          >
                            <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                          </svg>
                        ))}
                      </div>
                      <span className="text-sm text-gray-500">
                        {new Date(review.createdAt).toLocaleDateString()}
                      </span>
                    </div>

                    <h3 className="font-semibold text-gray-900 mb-2">
                      {review.title}
                    </h3>

                    <p className="text-gray-700 whitespace-pre-wrap">
                      {review.content}
                    </p>

                    {/* Review Images */}
                    {review.images && review.images.length > 0 && (
                      <div className="flex gap-2 mt-3">
                        {review.images.map((image) => (
                          <img
                            key={image.id}
                            src={image.imageUrl}
                            alt="Review"
                            className="w-20 h-20 object-cover rounded border cursor-pointer hover:opacity-75 transition"
                            onClick={() => window.open(image.imageUrl, "_blank")}
                          />
                        ))}
                      </div>
                    )}

                    {/* Seller Reply */}
                    {review.replyContent && (
                      <div className="mt-4 pl-4 border-l-2 border-blue-200 bg-blue-50 p-4 rounded">
                        <div className="flex items-center gap-2 mb-2">
                          <MessageSquare className="w-4 h-4 text-blue-600" />
                          <span className="font-semibold text-blue-900">
                            {review.repliedByName || "Seller"} replied
                          </span>
                        </div>
                        <p className="text-gray-700">{review.replyContent}</p>
                      </div>
                    )}

                    {/* Helpful Stats */}
                    <div className="mt-4 flex items-center gap-4 text-sm text-gray-600">
                      <span>{review.helpfulCount} found this helpful</span>
                      <span>•</span>
                      <span>{review.totalVotes} total votes</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* Pagination */}
            {!reviewsData.last && (
              <div className="mt-6 text-center">
                <button
                  onClick={() => setPage((prev) => prev + 1)}
                  disabled={isLoading}
                  className="px-6 py-3 bg-white border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 font-medium disabled:opacity-50 transition"
                >
                  {isLoading ? "Loading..." : "Load More"}
                </button>
              </div>
            )}
          </div>
        ) : (
          <div className="bg-white rounded-lg border p-12 text-center">
            <MessageSquare className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 mb-2">
              No reviews yet
            </h3>
            <p className="text-gray-600 mb-6">
              You haven't written any reviews. Purchase products and share your experience!
            </p>
            <button
              onClick={() => navigate("/products")}
              className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium transition"
            >
              Browse Products
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
