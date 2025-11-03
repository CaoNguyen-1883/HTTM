import { useEffect, useState } from "react";
import { useMLRecommendationsForYou, useMLSimilarProducts } from "../../lib/hooks/useRecommendations";
import { productsApi } from "../../lib/api/products.api";
import { Product } from "../../lib/types";
import { Loader2, Sparkles, ShoppingBag } from "lucide-react";
import { Link } from "react-router-dom";

interface MLRecommendedProductsProps {
  userId?: string;
  productId?: string;
  type: "user" | "similar";
  limit?: number;
  title?: string;
  description?: string;
}

/**
 * ML-Powered Recommendations Component
 * Uses Item-Based Collaborative Filtering
 */
export const MLRecommendedProducts = ({
  userId,
  productId,
  type,
  limit = 10,
  title,
  description,
}: MLRecommendedProductsProps) => {
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoadingProducts, setIsLoadingProducts] = useState(false);

  // Fetch ML recommendations
  const {
    data: userRecs,
    isLoading: isLoadingUserRecs,
    error: userRecsError,
  } = useMLRecommendationsForYou(userId, limit);

  const {
    data: similarRecs,
    isLoading: isLoadingSimilarRecs,
    error: similarRecsError,
  } = useMLSimilarProducts(productId, limit);

  const recommendations = type === "user" ? userRecs : similarRecs;
  const isLoadingRecs = type === "user" ? isLoadingUserRecs : isLoadingSimilarRecs;
  const error = type === "user" ? userRecsError : similarRecsError;

  // Fetch product details for recommended product IDs
  useEffect(() => {
    const fetchProducts = async () => {
      if (!recommendations || recommendations.recommendations.length === 0) {
        setProducts([]);
        return;
      }

      setIsLoadingProducts(true);
      try {
        // Fetch product details for each recommended product ID
        const productPromises = recommendations.recommendations.map(async (rec) => {
          try {
            const product = await productsApi.getProductById(rec.product_id);
            return product;
          } catch (error) {
            console.error(`Error fetching product ${rec.product_id}:`, error);
            return null;
          }
        });

        const fetchedProducts = await Promise.all(productPromises);
        // Filter out null values (failed fetches)
        const validProducts = fetchedProducts.filter((p): p is Product => p !== null);
        setProducts(validProducts);
      } catch (error) {
        console.error("Error fetching recommended products:", error);
        setProducts([]);
      } finally {
        setIsLoadingProducts(false);
      }
    };

    fetchProducts();
  }, [recommendations]);

  // Loading state
  if (isLoadingRecs || isLoadingProducts) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div className="flex items-center gap-2 mb-4">
          <Sparkles className="h-5 w-5 text-purple-500" />
          <h2 className="text-xl font-bold">{title || "AI Recommendations"}</h2>
          <span className="ml-2 px-2 py-1 text-xs font-medium bg-purple-100 text-purple-700 rounded-full flex items-center gap-1">
            <Sparkles className="h-3 w-3" />
            AI Powered
          </span>
        </div>
        {description && (
          <p className="text-sm text-gray-600 mb-4">{description}</p>
        )}
        <div className="flex items-center justify-center py-8">
          <Loader2 className="h-8 w-8 animate-spin text-gray-400" />
        </div>
      </div>
    );
  }

  // Error state
  if (error) {
    return (
      <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
        <p className="text-sm text-yellow-800">
          ML recommendations temporarily unavailable. Using fallback...
        </p>
      </div>
    );
  }

  // No recommendations
  if (!products || products.length === 0) {
    return null; // Don't show anything if no recommendations
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div className="flex items-center gap-2 mb-4">
        <Sparkles className="h-5 w-5 text-purple-500" />
        <h2 className="text-xl font-bold">
          {title || (type === "user" ? "Recommended For You" : "Customers Also Bought")}
        </h2>
        <span className="ml-2 px-2 py-1 text-xs font-medium bg-purple-100 text-purple-700 rounded-full flex items-center gap-1">
          <Sparkles className="h-3 w-3" />
          AI Powered
        </span>
      </div>
      {description && (
        <p className="text-sm text-gray-600 mb-6">{description}</p>
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
                {product.images && product.images.length > 0 ? (
                  <img
                    src={product.images[0].url}
                    alt={product.name}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform"
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
                <div className="mt-2 flex items-center justify-between">
                  <span className="text-lg font-bold text-blue-600">
                    ${product.basePrice.toFixed(2)}
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
          Showing {products.length} {type === "user" ? "personalized" : "similar"} recommendations
        </p>
      </div>
    </div>
  );
};
