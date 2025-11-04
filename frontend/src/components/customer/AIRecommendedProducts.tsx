import { Loader2, Sparkles, ShoppingBag, TrendingUp } from "lucide-react";
import { Link } from "react-router-dom";
import {
  usePersonalizedProducts,
  useSimilarProductsEnriched,
  usePopularProductsEnriched
} from "../../lib/hooks/useRecommendations";
import { ProductSummary } from "../../lib/types";

interface AIRecommendedProductsProps {
  type: "personalized" | "similar" | "popular";
  productId?: string; // Required for "similar" type
  limit?: number;
  title?: string;
  description?: string;
  showAIBadge?: boolean;
}

/**
 * 🤖 AI-Powered Product Recommendations Component
 * Uses Python ML API (Item-Based Collaborative Filtering) + Spring Boot for product details
 *
 * Types:
 * - "personalized": For logged-in users based on purchase history
 * - "similar": For product detail pages (customers also bought)
 * - "popular": Fallback for new users / guests
 */
export const AIRecommendedProducts = ({
  type,
  productId,
  limit = 10,
  title,
  description,
  showAIBadge = true,
}: AIRecommendedProductsProps) => {

  // Use enriched hooks - they handle EVERYTHING:
  // 1. Fetch purchase history (for personalized)
  // 2. Call Python ML API (get IDs + scores)
  // 3. Fetch product details from Spring Boot
  // 4. Convert to ProductSummary
  const {
    data: personalizedProducts,
    isLoading: isLoadingPersonalized,
    error: personalizedError
  } = usePersonalizedProducts(limit);

  const {
    data: similarProducts,
    isLoading: isLoadingSimilar,
    error: similarError
  } = useSimilarProductsEnriched(productId || "", limit);

  const {
    data: popularProducts,
    isLoading: isLoadingPopular,
    error: popularError
  } = usePopularProductsEnriched(limit);

  // Select data based on type
  let products: ProductSummary[] | undefined;
  let isLoading: boolean;
  let error: any;

  if (type === "personalized") {
    products = personalizedProducts;
    isLoading = isLoadingPersonalized;
    error = personalizedError;
  } else if (type === "similar") {
    products = similarProducts;
    isLoading = isLoadingSimilar;
    error = similarError;
  } else {
    products = popularProducts;
    isLoading = isLoadingPopular;
    error = popularError;
  }

  // Default titles
  const defaultTitle =
    type === "personalized" ? "Dành Cho Bạn" :
    type === "similar" ? "Khách Hàng Cũng Mua" :
    "Sản Phẩm Phổ Biến";

  // Loading state
  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div className="flex items-center gap-2 mb-4">
          {showAIBadge && <Sparkles className="h-5 w-5 text-purple-500" />}
          <h2 className="text-xl font-bold">{title || defaultTitle}</h2>
          {showAIBadge && (
            <span className="ml-2 px-2 py-1 text-xs font-medium bg-gradient-to-r from-purple-100 to-blue-100 text-purple-700 rounded-full flex items-center gap-1">
              <Sparkles className="h-3 w-3" />
              AI Powered
            </span>
          )}
        </div>
        {description && (
          <p className="text-sm text-gray-600 mb-4">{description}</p>
        )}
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-purple-500" />
        </div>
      </div>
    );
  }

  // Error state - Show fallback message
  if (error) {
    console.error(`AI Recommendations Error (${type}):`, error);
    return (
      <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
        <div className="flex items-center gap-2">
          <TrendingUp className="h-5 w-5 text-yellow-600" />
          <p className="text-sm text-yellow-800">
            Đang tải gợi ý từ hệ thống dự phòng...
          </p>
        </div>
      </div>
    );
  }

  // No recommendations - Don't show anything
  if (!products || products.length === 0) {
    return null;
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-2">
          {showAIBadge && <Sparkles className="h-5 w-5 text-purple-500" />}
          <h2 className="text-2xl font-bold">
            {title || defaultTitle}
          </h2>
          {showAIBadge && (
            <span className="px-3 py-1 text-xs font-medium bg-gradient-to-r from-purple-100 to-blue-100 text-purple-700 rounded-full flex items-center gap-1">
              <Sparkles className="h-3 w-3" />
              AI Powered
            </span>
          )}
        </div>
        <div className="text-sm text-gray-500">
          {products.length} sản phẩm
        </div>
      </div>

      {/* Description */}
      {description && (
        <p className="text-sm text-gray-600 mb-6">{description}</p>
      )}

      {/* Product Grid */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
        {products.map((product) => (
          <Link
            key={product.id}
            to={`/products/${product.id}`}
            className="group"
          >
            <div className="bg-white border border-gray-200 rounded-lg overflow-hidden hover:shadow-lg hover:border-purple-300 transition-all duration-200">
              {/* Product Image */}
              <div className="aspect-square relative overflow-hidden bg-gray-100">
                {product.primaryImage ? (
                  <img
                    src={product.primaryImage}
                    alt={product.name}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                    loading="lazy"
                  />
                ) : (
                  <div className="flex items-center justify-center h-full">
                    <ShoppingBag className="h-12 w-12 text-gray-400" />
                  </div>
                )}

                {/* Stock Badge */}
                {!product.hasStock && (
                  <div className="absolute top-2 right-2 bg-red-500 text-white text-xs px-2 py-1 rounded">
                    Hết hàng
                  </div>
                )}
              </div>

              {/* Product Info */}
              <div className="p-3">
                {/* Product Name */}
                <h3 className="font-medium text-sm line-clamp-2 group-hover:text-purple-600 transition-colors mb-2">
                  {product.name}
                </h3>

                {/* Brand & Category */}
                <div className="text-xs text-gray-500 mb-2">
                  {product.brandName}
                </div>

                {/* Price & Rating */}
                <div className="flex items-center justify-between">
                  <div className="flex flex-col">
                    {product.minPrice !== product.maxPrice ? (
                      <>
                        <span className="text-xs text-gray-500 line-through">
                          ${product.maxPrice.toFixed(2)}
                        </span>
                        <span className="text-lg font-bold text-purple-600">
                          ${product.minPrice.toFixed(2)}
                        </span>
                      </>
                    ) : (
                      <span className="text-lg font-bold text-purple-600">
                        ${product.basePrice.toFixed(2)}
                      </span>
                    )}
                  </div>

                  {product.averageRating > 0 && (
                    <div className="flex items-center gap-1">
                      <span className="text-yellow-500">⭐</span>
                      <span className="text-xs text-gray-600">
                        {product.averageRating.toFixed(1)}
                      </span>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </Link>
        ))}
      </div>

      {/* Footer Info */}
      <div className="mt-6 pt-4 border-t border-gray-200">
        <p className="text-xs text-center text-gray-500">
          {type === "personalized" && "🎯 Gợi ý dựa trên lịch sử mua hàng của bạn"}
          {type === "similar" && "🔗 Sản phẩm tương tự được mua cùng nhau"}
          {type === "popular" && "🔥 Sản phẩm được yêu thích nhất"}
        </p>
      </div>
    </div>
  );
};
