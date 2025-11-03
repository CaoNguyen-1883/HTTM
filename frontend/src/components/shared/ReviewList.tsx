import { useState } from "react";
import { ChevronDown, Filter } from "lucide-react";
import { ReviewItem } from "./ReviewItem";
import { Review } from "../../lib/types";

interface ReviewListProps {
  reviews: Review[];
  isLoading?: boolean;
  showProductName?: boolean;
  onReply?: (reviewId: string) => void;
  canReply?: boolean;
  onLoadMore?: () => void;
  hasMore?: boolean;
  totalReviews?: number;
}

export const ReviewList = ({
  reviews,
  isLoading = false,
  showProductName = false,
  onReply,
  canReply = false,
  onLoadMore,
  hasMore = false,
  totalReviews,
}: ReviewListProps) => {
  const [sortBy, setSortBy] = useState<"recent" | "helpful" | "rating">("recent");

  if (isLoading && reviews.length === 0) {
    return (
      <div className="space-y-4">
        {[1, 2, 3].map((i) => (
          <div
            key={i}
            className="bg-white border rounded-lg p-6 animate-pulse"
          >
            <div className="flex items-start gap-3 mb-4">
              <div className="w-10 h-10 rounded-full bg-gray-200" />
              <div className="flex-1">
                <div className="h-4 bg-gray-200 rounded w-32 mb-2" />
                <div className="h-3 bg-gray-200 rounded w-48" />
              </div>
            </div>
            <div className="h-4 bg-gray-200 rounded w-full mb-2" />
            <div className="h-4 bg-gray-200 rounded w-3/4" />
          </div>
        ))}
      </div>
    );
  }

  if (reviews.length === 0) {
    return (
      <div className="text-center py-12 bg-white rounded-lg border">
        <p className="text-gray-500">No reviews yet. Be the first to review!</p>
      </div>
    );
  }

  return (
    <div>
      {/* Header with sort options */}
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-gray-900">
          {totalReviews !== undefined && `${totalReviews} Reviews`}
        </h3>

        <div className="flex items-center gap-2">
          <Filter className="w-4 h-4 text-gray-500" />
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as any)}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="recent">Most Recent</option>
            <option value="helpful">Most Helpful</option>
            <option value="rating">Highest Rating</option>
          </select>
        </div>
      </div>

      {/* Reviews */}
      <div className="bg-white rounded-lg border divide-y">
        {reviews.map((review) => (
          <ReviewItem
            key={review.id}
            review={review}
            showProductName={showProductName}
            onReply={onReply}
            canReply={canReply}
          />
        ))}
      </div>

      {/* Load More */}
      {hasMore && onLoadMore && (
        <div className="mt-6 text-center">
          <button
            onClick={onLoadMore}
            disabled={isLoading}
            className="inline-flex items-center gap-2 px-6 py-3 bg-white border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 font-medium disabled:opacity-50 transition"
          >
            {isLoading ? (
              <>
                <div className="w-5 h-5 border-2 border-gray-300 border-t-gray-600 rounded-full animate-spin" />
                Loading...
              </>
            ) : (
              <>
                <ChevronDown className="w-5 h-5" />
                Load More Reviews
              </>
            )}
          </button>
        </div>
      )}
    </div>
  );
};
