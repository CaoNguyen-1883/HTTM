import { Star } from "lucide-react";
import { ProductRatingSummary as ProductRatingSummaryType } from "../../lib/types";
import { RatingStars } from "./RatingStars";

interface ProductRatingSummaryProps {
  summary: ProductRatingSummaryType;
  onFilterByRating?: (rating: number) => void;
}

export const ProductRatingSummary = ({
  summary,
  onFilterByRating,
}: ProductRatingSummaryProps) => {
  const ratingBreakdown = [
    { stars: 5, count: summary.fiveStarCount, percentage: summary.fiveStarPercentage },
    { stars: 4, count: summary.fourStarCount, percentage: summary.fourStarPercentage },
    { stars: 3, count: summary.threeStarCount, percentage: summary.threeStarPercentage },
    { stars: 2, count: summary.twoStarCount, percentage: summary.twoStarPercentage },
    { stars: 1, count: summary.oneStarCount, percentage: summary.oneStarPercentage },
  ];

  return (
    <div className="bg-white rounded-lg border p-6">
      {/* Overall Rating */}
      <div className="text-center pb-6 border-b">
        <div className="text-5xl font-bold text-gray-900 mb-2">
          {summary.averageRating.toFixed(1)}
        </div>
        <RatingStars rating={summary.averageRating} size="lg" />
        <div className="text-sm text-gray-600 mt-2">
          Based on {summary.totalReviews} {summary.totalReviews === 1 ? "review" : "reviews"}
        </div>
      </div>

      {/* Rating Breakdown */}
      <div className="mt-6 space-y-3">
        {ratingBreakdown.map(({ stars, count, percentage }) => (
          <button
            key={stars}
            onClick={() => onFilterByRating?.(stars)}
            className="w-full flex items-center gap-3 hover:bg-gray-50 p-2 rounded transition-colors"
          >
            <div className="flex items-center gap-1 min-w-[60px]">
              <span className="text-sm font-medium text-gray-700">{stars}</span>
              <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
            </div>

            {/* Progress Bar */}
            <div className="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden">
              <div
                className="h-full bg-yellow-400 transition-all"
                style={{ width: `${percentage}%` }}
              />
            </div>

            {/* Count */}
            <span className="text-sm text-gray-600 min-w-[60px] text-right">
              ({count})
            </span>
          </button>
        ))}
      </div>
    </div>
  );
};
