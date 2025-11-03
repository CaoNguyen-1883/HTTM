import { Star } from "lucide-react";

interface RatingStarsProps {
  rating: number;
  maxRating?: number;
  size?: "sm" | "md" | "lg";
  showNumber?: boolean;
  interactive?: boolean;
  onRatingChange?: (rating: number) => void;
}

export const RatingStars = ({
  rating,
  maxRating = 5,
  size = "md",
  showNumber = false,
  interactive = false,
  onRatingChange,
}: RatingStarsProps) => {
  const sizeClasses = {
    sm: "w-4 h-4",
    md: "w-5 h-5",
    lg: "w-6 h-6",
  };

  const stars = [];
  for (let i = 1; i <= maxRating; i++) {
    const diff = rating - i;

    if (diff >= 0) {
      // Full star
      stars.push(
        <Star
          key={i}
          className={`${sizeClasses[size]} fill-yellow-400 text-yellow-400 ${
            interactive ? "cursor-pointer hover:scale-110 transition-transform" : ""
          }`}
          onClick={() => interactive && onRatingChange?.(i)}
        />
      );
    } else if (diff > -1 && diff < 0) {
      // Half star
      stars.push(
        <div key={i} className="relative inline-block">
          <Star
            className={`${sizeClasses[size]} text-yellow-400`}
            onClick={() => interactive && onRatingChange?.(i)}
          />
          <div className="absolute inset-0 overflow-hidden" style={{ width: "50%" }}>
            <Star
              className={`${sizeClasses[size]} fill-yellow-400 text-yellow-400`}
              onClick={() => interactive && onRatingChange?.(i)}
            />
          </div>
        </div>
      );
    } else {
      // Empty star
      stars.push(
        <Star
          key={i}
          className={`${sizeClasses[size]} text-gray-300 ${
            interactive ? "cursor-pointer hover:scale-110 transition-transform" : ""
          }`}
          onClick={() => interactive && onRatingChange?.(i)}
        />
      );
    }
  }

  return (
    <div className="flex items-center gap-1">
      {stars}
      {showNumber && (
        <span className="ml-1 text-sm text-gray-600">
          {rating.toFixed(1)}
        </span>
      )}
    </div>
  );
};
