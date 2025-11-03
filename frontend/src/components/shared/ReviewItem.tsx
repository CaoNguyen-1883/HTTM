import { useState } from "react";
import { ThumbsUp, ThumbsDown, CheckCircle, MessageSquare, Calendar } from "lucide-react";
import { format } from "date-fns";
import { Review } from "../../lib/types";
import { RatingStars } from "./RatingStars";
import { useVoteReview, useRemoveVote } from "../../lib/hooks";

interface ReviewItemProps {
  review: Review;
  showProductName?: boolean;
  onReply?: (reviewId: string) => void;
  canReply?: boolean;
}

export const ReviewItem = ({
  review,
  showProductName = false,
  onReply,
  canReply = false,
}: ReviewItemProps) => {
  const voteReviewMutation = useVoteReview();
  const removeVoteMutation = useRemoveVote();
  const [showFullContent, setShowFullContent] = useState(false);

  const contentPreviewLength = 300;
  const shouldTruncate = review.content.length > contentPreviewLength;
  const displayContent = showFullContent || !shouldTruncate
    ? review.content
    : review.content.substring(0, contentPreviewLength) + "...";

  const handleVote = async (isHelpful: boolean) => {
    if (review.userVote === isHelpful) {
      // Remove vote if clicking the same button
      await removeVoteMutation.mutateAsync(review.id);
    } else {
      // Vote or change vote
      await voteReviewMutation.mutateAsync({ reviewId: review.id, isHelpful });
    }
  };

  return (
    <div className="bg-white border-b last:border-b-0 p-6">
      {/* Header */}
      <div className="flex items-start justify-between mb-3">
        <div className="flex items-start gap-3">
          {/* User Avatar */}
          <div className="flex-shrink-0">
            {review.userAvatar ? (
              <img
                src={review.userAvatar}
                alt={review.userName}
                className="w-10 h-10 rounded-full object-cover"
              />
            ) : (
              <div className="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center">
                <span className="text-gray-600 font-medium text-sm">
                  {review.userName.charAt(0).toUpperCase()}
                </span>
              </div>
            )}
          </div>

          {/* User Info */}
          <div>
            <div className="flex items-center gap-2">
              <h4 className="font-semibold text-gray-900">{review.userName}</h4>
              {review.isVerifiedPurchase && (
                <span className="inline-flex items-center gap-1 px-2 py-0.5 bg-green-50 text-green-700 text-xs rounded-full">
                  <CheckCircle className="w-3 h-3" />
                  Verified Purchase
                </span>
              )}
            </div>
            <div className="flex items-center gap-2 mt-1">
              <RatingStars rating={review.rating} size="sm" />
              <span className="text-xs text-gray-500">
                {format(new Date(review.createdAt), "MMM dd, yyyy")}
              </span>
            </div>
          </div>
        </div>

        {/* Product Name (if showing) */}
        {showProductName && (
          <div className="text-sm text-gray-600">
            Review for: <span className="font-medium">{review.productName}</span>
          </div>
        )}
      </div>

      {/* Review Title */}
      <h3 className="font-semibold text-gray-900 mb-2">{review.title}</h3>

      {/* Review Content */}
      <div className="text-gray-700 mb-3 whitespace-pre-wrap">
        {displayContent}
        {shouldTruncate && !showFullContent && (
          <button
            onClick={() => setShowFullContent(true)}
            className="text-blue-600 hover:text-blue-700 ml-1"
          >
            Read more
          </button>
        )}
        {showFullContent && shouldTruncate && (
          <button
            onClick={() => setShowFullContent(false)}
            className="text-blue-600 hover:text-blue-700 ml-1"
          >
            Show less
          </button>
        )}
      </div>

      {/* Review Images */}
      {review.images && review.images.length > 0 && (
        <div className="flex gap-2 mb-4">
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

      {/* Helpful Votes */}
      <div className="flex items-center gap-4 mb-3">
        <span className="text-sm text-gray-600">Was this helpful?</span>

        <button
          onClick={() => handleVote(true)}
          disabled={voteReviewMutation.isPending || removeVoteMutation.isPending}
          className={`flex items-center gap-1 px-3 py-1 rounded border transition ${
            review.userVote === true
              ? "bg-blue-50 border-blue-300 text-blue-700"
              : "border-gray-300 text-gray-700 hover:bg-gray-50"
          } disabled:opacity-50`}
        >
          <ThumbsUp className="w-4 h-4" />
          <span className="text-sm">
            {review.userVote === true ? "Helpful" : "Yes"} ({review.helpfulCount})
          </span>
        </button>

        <button
          onClick={() => handleVote(false)}
          disabled={voteReviewMutation.isPending || removeVoteMutation.isPending}
          className={`flex items-center gap-1 px-3 py-1 rounded border transition ${
            review.userVote === false
              ? "bg-red-50 border-red-300 text-red-700"
              : "border-gray-300 text-gray-700 hover:bg-gray-50"
          } disabled:opacity-50`}
        >
          <ThumbsDown className="w-4 h-4" />
          <span className="text-sm">
            {review.userVote === false ? "Not helpful" : "No"} ({review.notHelpfulCount})
          </span>
        </button>

        {canReply && onReply && (
          <button
            onClick={() => onReply(review.id)}
            className="ml-auto flex items-center gap-1 px-3 py-1 rounded border border-gray-300 text-gray-700 hover:bg-gray-50 transition"
          >
            <MessageSquare className="w-4 h-4" />
            <span className="text-sm">Reply</span>
          </button>
        )}
      </div>

      {/* Seller Reply */}
      {review.replyContent && (
        <div className="mt-4 ml-13 pl-4 border-l-2 border-blue-200 bg-blue-50 p-4 rounded">
          <div className="flex items-center gap-2 mb-2">
            <MessageSquare className="w-4 h-4 text-blue-600" />
            <span className="font-semibold text-blue-900">
              {review.repliedByName || "Seller"} replied
            </span>
            {review.repliedAt && (
              <span className="text-xs text-gray-500 flex items-center gap-1">
                <Calendar className="w-3 h-3" />
                {format(new Date(review.repliedAt), "MMM dd, yyyy")}
              </span>
            )}
          </div>
          <p className="text-gray-700">{review.replyContent}</p>
        </div>
      )}
    </div>
  );
};
