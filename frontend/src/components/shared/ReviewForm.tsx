import { useState } from "react";
import { Star, X, Upload, Loader2 } from "lucide-react";
import { CreateReviewRequest, UpdateReviewRequest } from "../../lib/types";

interface ReviewFormProps {
  productId: string;
  orderId?: string;
  initialData?: {
    rating: number;
    title: string;
    content: string;
    imageUrls?: string[];
  };
  onSubmit: (data: CreateReviewRequest | UpdateReviewRequest) => Promise<void>;
  onCancel: () => void;
  isSubmitting?: boolean;
  mode?: "create" | "edit";
}

export const ReviewForm = ({
  productId,
  orderId,
  initialData,
  onSubmit,
  onCancel,
  isSubmitting = false,
  mode = "create",
}: ReviewFormProps) => {
  const [rating, setRating] = useState(initialData?.rating || 0);
  const [hoverRating, setHoverRating] = useState(0);
  const [title, setTitle] = useState(initialData?.title || "");
  const [content, setContent] = useState(initialData?.content || "");
  const [imageUrls, setImageUrls] = useState<string[]>(initialData?.imageUrls || []);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (rating === 0) {
      newErrors.rating = "Please select a rating";
    }

    if (!title.trim()) {
      newErrors.title = "Title is required";
    } else if (title.length > 100) {
      newErrors.title = "Title must not exceed 100 characters";
    }

    if (!content.trim()) {
      newErrors.content = "Review content is required";
    } else if (content.length < 10) {
      newErrors.content = "Review must be at least 10 characters";
    } else if (content.length > 2000) {
      newErrors.content = "Review must not exceed 2000 characters";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    const data = mode === "create"
      ? {
          productId,
          orderId,
          rating,
          title: title.trim(),
          content: content.trim(),
          imageUrls: imageUrls.length > 0 ? imageUrls : undefined,
        }
      : {
          rating,
          title: title.trim(),
          content: content.trim(),
          imageUrls: imageUrls.length > 0 ? imageUrls : undefined,
        };

    await onSubmit(data as any);
  };

  const handleAddImage = (url: string) => {
    if (url.trim() && imageUrls.length < 5) {
      setImageUrls([...imageUrls, url.trim()]);
    }
  };

  const handleRemoveImage = (index: number) => {
    setImageUrls(imageUrls.filter((_, i) => i !== index));
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white rounded-lg border p-6">
      <h3 className="text-xl font-bold text-gray-900 mb-6">
        {mode === "create" ? "Write a Review" : "Edit Your Review"}
      </h3>

      {/* Rating */}
      <div className="mb-6">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Rating <span className="text-red-500">*</span>
        </label>
        <div className="flex gap-2">
          {[1, 2, 3, 4, 5].map((star) => (
            <button
              key={star}
              type="button"
              onClick={() => setRating(star)}
              onMouseEnter={() => setHoverRating(star)}
              onMouseLeave={() => setHoverRating(0)}
              className="focus:outline-none transition-transform hover:scale-110"
            >
              <Star
                className={`w-8 h-8 ${
                  star <= (hoverRating || rating)
                    ? "fill-yellow-400 text-yellow-400"
                    : "text-gray-300"
                }`}
              />
            </button>
          ))}
          {rating > 0 && (
            <span className="ml-2 text-sm text-gray-600 self-center">
              {rating === 5 && "Excellent!"}
              {rating === 4 && "Good"}
              {rating === 3 && "Average"}
              {rating === 2 && "Poor"}
              {rating === 1 && "Terrible"}
            </span>
          )}
        </div>
        {errors.rating && (
          <p className="text-red-500 text-sm mt-1">{errors.rating}</p>
        )}
      </div>

      {/* Title */}
      <div className="mb-6">
        <label
          htmlFor="title"
          className="block text-sm font-medium text-gray-700 mb-2"
        >
          Review Title <span className="text-red-500">*</span>
        </label>
        <input
          type="text"
          id="title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="Summarize your review in one sentence"
          maxLength={100}
          className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
            errors.title ? "border-red-500" : "border-gray-300"
          }`}
        />
        <div className="flex justify-between mt-1">
          {errors.title ? (
            <p className="text-red-500 text-sm">{errors.title}</p>
          ) : (
            <span />
          )}
          <span className="text-sm text-gray-500">{title.length}/100</span>
        </div>
      </div>

      {/* Content */}
      <div className="mb-6">
        <label
          htmlFor="content"
          className="block text-sm font-medium text-gray-700 mb-2"
        >
          Your Review <span className="text-red-500">*</span>
        </label>
        <textarea
          id="content"
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="Share your thoughts about this product..."
          rows={6}
          maxLength={2000}
          className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
            errors.content ? "border-red-500" : "border-gray-300"
          }`}
        />
        <div className="flex justify-between mt-1">
          {errors.content ? (
            <p className="text-red-500 text-sm">{errors.content}</p>
          ) : (
            <p className="text-sm text-gray-500">
              Minimum 10 characters
            </p>
          )}
          <span className="text-sm text-gray-500">{content.length}/2000</span>
        </div>
      </div>

      {/* Images */}
      <div className="mb-6">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Add Photos (Optional)
        </label>
        <p className="text-sm text-gray-500 mb-3">
          Add up to 5 photos to help others see what you experienced
        </p>

        {/* Image List */}
        {imageUrls.length > 0 && (
          <div className="flex flex-wrap gap-2 mb-3">
            {imageUrls.map((url, index) => (
              <div key={index} className="relative group">
                <img
                  src={url}
                  alt={`Review ${index + 1}`}
                  className="w-20 h-20 object-cover rounded border"
                />
                <button
                  type="button"
                  onClick={() => handleRemoveImage(index)}
                  className="absolute -top-2 -right-2 p-1 bg-red-500 text-white rounded-full opacity-0 group-hover:opacity-100 transition"
                >
                  <X className="w-3 h-3" />
                </button>
              </div>
            ))}
          </div>
        )}

        {/* Add Image */}
        {imageUrls.length < 5 && (
          <div className="flex gap-2">
            <input
              type="url"
              placeholder="Enter image URL"
              onKeyPress={(e) => {
                if (e.key === "Enter") {
                  e.preventDefault();
                  handleAddImage((e.target as HTMLInputElement).value);
                  (e.target as HTMLInputElement).value = "";
                }
              }}
              className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
            <button
              type="button"
              onClick={(e) => {
                const input = e.currentTarget.previousElementSibling as HTMLInputElement;
                handleAddImage(input.value);
                input.value = "";
              }}
              className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition"
            >
              <Upload className="w-5 h-5" />
            </button>
          </div>
        )}
      </div>

      {/* Actions */}
      <div className="flex gap-3 justify-end pt-4 border-t">
        <button
          type="button"
          onClick={onCancel}
          disabled={isSubmitting}
          className="px-6 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 font-medium disabled:opacity-50 transition"
        >
          Cancel
        </button>
        <button
          type="submit"
          disabled={isSubmitting}
          className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium disabled:opacity-50 transition flex items-center gap-2"
        >
          {isSubmitting ? (
            <>
              <Loader2 className="w-5 h-5 animate-spin" />
              {mode === "create" ? "Submitting..." : "Updating..."}
            </>
          ) : (
            <>{mode === "create" ? "Submit Review" : "Update Review"}</>
          )}
        </button>
      </div>
    </form>
  );
};
