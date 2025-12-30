import { useSimilarProducts } from "../../lib/hooks/useRecommendations";
import { Loader2, Sparkles, ShoppingBag } from "lucide-react";
import { Link } from "react-router-dom";

interface MLRecommendedProductsProps {
  productId?: string;
  limit?: number;
  title?: string;
  description?: string;
}

/**
 * ML-Powered Similar Products Component
 * Uses backend recommendation service
 */
export const MLRecommendedProducts = ({
  productId,
  limit = 10,
  title,
  description,
}: MLRecommendedProductsProps) => {
  // Fetch similar products (returns full RecommendationSection with products)
  const {
    data: recommendationSection,
    isLoading,
    error,
  } = useSimilarProducts(productId || "", limit);

  const products = recommendationSection?.products;
  const sectionTitle = recommendationSection?.sectionTitle;
  const sectionDescription = recommendationSection?.sectionDescription;

  // Loading state
  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div className="flex items-center gap-2 mb-4">
          <Sparkles className="h-5 w-5 text-purple-500" />
          <h2 className="text-xl font-bold">{title || "Similar Products"}</h2>
        </div>
        <div className="flex items-center justify-center py-8">
          <Loader2 className="h-8 w-8 animate-spin text-gray-400" />
        </div>
      </div>
    );
  }

  // Error state
  if (error) {
    return null; // Don't show if error
  }

  // No recommendations
  if (!products || products.length === 0) {
    return null;
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div className="flex items-center gap-2 mb-4">
        <h2 className="text-xl font-bold">
          {title || sectionTitle || "Similar Products"}
        </h2>
      </div>

      {(description || sectionDescription) && (
        <p className="text-sm text-gray-600 mb-6">
          {description || sectionDescription}
        </p>
      )}

      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
        {products.map((product) => (
          <Link
            key={product.id}
            to={`/products/${product.id}`}
            className="group"
          >
            <div className="bg-white border border-gray-200 rounded-lg overflow-hidden hover:shadow-lg transition-shadow">
              <div className="aspect-square relative overflow-hidden bg-gray-100">
                {product.primaryImage ? (
                  <img
                    src={product.primaryImage}
                    alt={product.name}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform"
                    loading="lazy"
                  />
                ) : (
                  <div className="flex items-center justify-center h-full">
                    <ShoppingBag className="h-12 w-12 text-gray-400" />
                  </div>
                )}
              </div>
              <div className="p-3">
                <h3 className="font-medium text-sm line-clamp-2 group-hover:text-blue-600 transition-colors">
                  {product.name}
                </h3>
                <p className="text-xs text-gray-500 mt-1">
                  {product.brandName}
                </p>
                <div className="mt-2 flex items-center justify-between">
                  <span className="text-lg font-bold text-blue-600">
                    ${product.basePrice?.toFixed(2)}
                  </span>
                  {product.averageRating && product.averageRating > 0 && (
                    <span className="text-xs text-gray-500">
                      ⭐ {product.averageRating.toFixed(1)}
                    </span>
                  )}
                </div>
              </div>
            </div>
          </Link>
        ))}
      </div>

      {/* Show count */}
      <div className="mt-4 text-center">
        <p className="text-sm text-gray-500">
          Showing {products.length} similar products
        </p>
      </div>
    </div>
  );
};
