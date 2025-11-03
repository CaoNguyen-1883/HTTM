import { Link, useNavigate } from "react-router-dom";
import { ProductSummary } from "../../lib/types";
import { formatCurrency } from "../../lib/utils";
import { ShoppingCart } from "lucide-react";
import { useAuthStore } from "../../lib/stores/authStore";
import { useAddToCart } from "../../lib/hooks";
import { useState } from "react";

interface ProductCardProps {
  product: ProductSummary;
}

export const ProductCard = ({ product }: ProductCardProps) => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuthStore();
  const addToCartMutation = useAddToCart();
  const [isAdding, setIsAdding] = useState(false);

  const discount =
    product.basePrice > product.minPrice
      ? Math.round(((product.basePrice - product.minPrice) / product.basePrice) * 100)
      : 0;

  const handleQuickAddToCart = async (e: React.MouseEvent) => {
    e.preventDefault(); // Prevent navigation to product detail
    e.stopPropagation();

    if (!isAuthenticated) {
      navigate("/login", { state: { from: `/products/${product.id}` } });
      return;
    }

    if (!product.hasStock) {
      alert("Out of stock");
      return;
    }

    // Use the first variant ID (we need to get this from product detail)
    // For now, we'll navigate to product detail if variant selection is needed
    if (!product.defaultVariantId) {
      navigate(`/products/${product.id}`);
      return;
    }

    setIsAdding(true);
    try {
      await addToCartMutation.mutateAsync({
        variantId: product.defaultVariantId,
        quantity: 1,
      });
      alert("Add to card sucessfully!");
    } catch (error: any) {
      const errorMsg = error.response?.data?.message || "Can not add to cart";
      alert(errorMsg);
    } finally {
      setIsAdding(false);
    }
  };

  return (
    <div className="group relative bg-white rounded-lg border border-gray-200 overflow-hidden hover:shadow-lg transition-shadow duration-200">
      <Link to={`/products/${product.id}`} className="block">
        {/* Image */}
        <div className="relative aspect-square overflow-hidden bg-gray-100">
          <img
            src={product.primaryImage || "/placeholder-product.png"}
            alt={product.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-200"
          />

          {/* Discount Badge */}
          {discount > 0 && (
            <div className="absolute top-2 left-2 bg-red-500 text-white text-xs font-bold px-2 py-1 rounded">
              -{discount}%
            </div>
          )}

          {/* Out of Stock Badge */}
          {!product.hasStock && (
            <div className="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center">
              <span className="text-white font-semibold">Hết hàng</span>
            </div>
          )}

          {/* Quick Add Button - Show on hover */}
          {product.hasStock && (
            <div className="absolute bottom-0 left-0 right-0 p-3 bg-gradient-to-t from-black/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-200">
              <button
                onClick={handleQuickAddToCart}
                disabled={isAdding}
                className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <ShoppingCart className="w-4 h-4" />
                {isAdding ? "Đang thêm..." : "Thêm vào giỏ"}
              </button>
            </div>
          )}
        </div>

        {/* Content */}
        <div className="p-4">
          {/* Category & Brand */}
          <div className="flex items-center gap-2 text-xs text-gray-500 mb-1">
            <span>{product.categoryName}</span>
            <span>•</span>
            <span>{product.brandName}</span>
          </div>

          {/* Product Name */}
          <h3 className="font-medium text-gray-900 line-clamp-2 mb-2 group-hover:text-blue-600 transition-colors">
            {product.name}
          </h3>

          {/* Rating */}
          <div className="flex items-center gap-1 mb-2">
            <div className="flex items-center">
              {[...Array(5)].map((_, i) => (
                <svg
                  key={i}
                  className={`w-4 h-4 ${
                    i < Math.floor(product.averageRating)
                      ? "text-yellow-400"
                      : "text-gray-300"
                  }`}
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                </svg>
              ))}
            </div>
            <span className="text-sm text-gray-600">
              {product.averageRating != null ? product.averageRating.toFixed(1) : '0.0'} ({product.totalReviews || 0})
            </span>
          </div>

          {/* Price */}
          <div className="flex items-baseline gap-2">
            {product.minPrice === product.maxPrice ? (
              <span className="text-lg font-bold text-gray-900">
                {formatCurrency(product.minPrice)}
              </span>
            ) : (
              <>
                <span className="text-lg font-bold text-gray-900">
                  {formatCurrency(product.minPrice)}
                </span>
                <span className="text-sm text-gray-500">
                  - {formatCurrency(product.maxPrice)}
                </span>
              </>
            )}

            {discount > 0 && (
              <span className="text-sm text-gray-400 line-through">
                {formatCurrency(product.basePrice)}
              </span>
            )}
          </div>

          {/* Stock */}
          <div className="mt-2 text-xs text-gray-500">
             {product.totalStock} {product.totalStock == 1 ? "stock" : "stocks"}
          </div>
        </div>
      </Link>
    </div>
  );
};
