import { useParams, useNavigate, Link } from "react-router-dom";
import { useState, useEffect } from "react";
import { Star, ShoppingCart, Heart, Share2, ChevronRight, MessageCircle } from "lucide-react";
import { useProduct } from "../../lib/hooks/useProducts";
import { useCartStore } from "../../lib/stores/cartStore";
import { useAuthStore } from "../../lib/stores/authStore";
import { useProductReviews, useProductRatingSummary, useCreateReview } from "../../lib/hooks";
import { ImageGallery } from "../../components/shared/ImageGallery";
import { VariantSelector } from "../../components/shared/VariantSelector";
import { QuantitySelector } from "../../components/shared/QuantitySelector";
import { ProductRatingSummary } from "../../components/shared/ProductRatingSummary";
import { ReviewList } from "../../components/shared/ReviewList";
import { ReviewForm } from "../../components/shared/ReviewForm";
import { MLRecommendedProducts } from "../../components/customer/MLRecommendedProducts";
import { Button } from "../../components/ui/Button";
import { ProductVariant, CreateReviewRequest } from "../../lib/types";
import { formatCurrency } from "../../lib/utils";

export const ProductDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { data: product, isLoading, error } = useProduct(id!);
  const { addToCart, isLoading: isAddingToCart } = useCartStore();
  const { isAuthenticated } = useAuthStore();


  const [selectedVariant, setSelectedVariant] = useState<ProductVariant | null>(
    null
  );
  const [quantity, setQuantity] = useState(1);
  const [activeTab, setActiveTab] = useState<"description" | "specs" | "reviews">(
    "description"
  );
  const [showReviewForm, setShowReviewForm] = useState(false);
  const [reviewPage, setReviewPage] = useState(0);

  // Reviews data
  const { data: reviewsData, isLoading: reviewsLoading } = useProductReviews(
    id!,
    { page: reviewPage, size: 10 }
  );
  const { data: ratingSummary } = useProductRatingSummary(id!);
  const createReviewMutation = useCreateReview();

  // Auto-select first available variant
  useEffect(() => {
    if (product?.variants && product.variants.length > 0 && !selectedVariant) {
      const firstAvailable = product.variants.find((v) => v.stock > 0);
      if (firstAvailable) {
        setSelectedVariant(firstAvailable);
      }
    }
  }, [product, selectedVariant]);

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      navigate("/login", { state: { from: `/products/${id}` } });
      return;
    }

    if (!selectedVariant) {
      alert("Please select a product variant");
      return;
    }

    try {
      await addToCart({
        variantId: selectedVariant.id,
        quantity,
      });
      // Show success message (you can use toast notification here)
      alert("Added to cart!");
    } catch (error) {
      console.error("Failed to add to cart:", error);
    }
  };

  const calculateFinalPrice = () => {
    if (selectedVariant) {
      return selectedVariant.price;
    }
    return product?.basePrice || 0;
  };

  const maxQuantity = selectedVariant?.availableStock || selectedVariant?.stock || 0;


  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="animate-pulse space-y-6">
          <div className="h-8 bg-gray-200 rounded w-1/3"></div>
          <div className="grid md:grid-cols-2 gap-8">
            <div className="aspect-square bg-gray-200 rounded-lg"></div>
            <div className="space-y-4">
              <div className="h-10 bg-gray-200 rounded"></div>
              <div className="h-6 bg-gray-200 rounded w-2/3"></div>
              <div className="h-4 bg-gray-200 rounded w-1/2"></div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">
            Product not found
          </h2>
          <Button onClick={() => navigate("/products")}>
            Back to products
          </Button>
        </div>
      </div>
    );
  }

  const finalPrice = calculateFinalPrice();
  const hasDiscount = product.basePrice > finalPrice;
  const discountPercent = hasDiscount
    ? Math.round(((product.basePrice - finalPrice) / product.basePrice) * 100)
    : 0;

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Breadcrumb */}
      <nav className="flex items-center gap-2 text-sm text-gray-500 mb-6">
        <Link to="/" className="hover:text-gray-700">
          Home
        </Link>
        <ChevronRight className="w-4 h-4" />
        <Link to="/products" className="hover:text-gray-700">
          Products
        </Link>
        <ChevronRight className="w-4 h-4" />
        <Link
          to={`/products?categoryId=${product.category.id}`}
          className="hover:text-gray-700"
        >
          {product.category.name}
        </Link>
        <ChevronRight className="w-4 h-4" />
        <span className="text-gray-900 font-medium">{product.name}</span>
      </nav>

      {/* Product Detail */}
      <div className="grid md:grid-cols-2 gap-8 lg:gap-12 mb-12">
        {/* Left: Image Gallery */}
        <div>
          <ImageGallery images={product.images} productName={product.name} />
        </div>

        {/* Right: Product Info */}
        <div className="space-y-6">
          {/* Product Name */}
          <div>
            <h1 className="text-3xl font-bold text-gray-900 mb-2">
              {product.name}
            </h1>
            <p className="text-gray-600">{product.shortDescription}</p>
          </div>

          {/* Rating & Stats */}
          <div className="flex items-center gap-4 text-sm">
            <div className="flex items-center gap-1">
              <Star className="w-5 h-5 fill-yellow-400 text-yellow-400" />
              <span className="font-semibold">{product.averageRating}</span>
              <span className="text-gray-500">
                ({product.totalReviews} reviews)
              </span>
            </div>
            <div className="h-4 w-px bg-gray-300"></div>
            <span className="text-gray-600">
              Sold: {product.purchaseCount}
            </span>
            <div className="h-4 w-px bg-gray-300"></div>
            <span className="text-gray-600">
              Views: {product.viewCount}
            </span>
          </div>

          {/* Price */}
          <div className="bg-gray-50 rounded-lg p-4">
            <div className="flex items-baseline gap-3">
              <span className="text-3xl font-bold text-blue-600">
                ${finalPrice}
              </span>
              {hasDiscount && (
                <>
                  <span className="text-lg text-gray-400 line-through">
                    ${product.basePrice}
                  </span>
                  <span className="bg-red-100 text-red-600 px-2 py-1 rounded text-sm font-semibold">
                    -{discountPercent}%
                  </span>
                </>
              )}
            </div>
          </div>

          {/* Brand & Category */}
          <div className="flex gap-4 text-sm">
            <div>
              <span className="text-gray-500">Brand: </span>
              <Link
                to={`/products?brandId=${product.brand.id}`}
                className="text-blue-600 hover:underline font-medium"
              >
                {product.brand.name}
              </Link>
            </div>
            <div className="h-4 w-px bg-gray-300"></div>
            <div>
              <span className="text-gray-500">Category: </span>
              <Link
                to={`/products?categoryId=${product.category.id}`}
                className="text-blue-600 hover:underline font-medium"
              >
                {product.category.name}
              </Link>
            </div>
          </div>

          {/* Variant Selector */}
          {product.variants && product.variants.length > 0 && (
            <VariantSelector
              variants={product.variants}
              selectedVariant={selectedVariant}
              onVariantChange={setSelectedVariant}
            />
          )}

          {/* Quantity Selector */}
          {selectedVariant && selectedVariant.stock > 0 && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Quantity
              </label>
              <QuantitySelector
                value={quantity}
                min={1}
                max={maxQuantity}
                onChange={setQuantity}
              />
            </div>
          )}

          {/* Stock Status */}
          <div className="text-sm">
            {product.totalStock === 0 ? (
              <span className="text-red-600 font-semibold">Out of stock</span>
            ) : product.totalStock < 10 ? (
              <span className="text-orange-600 font-semibold">
                Only {product.totalStock} left
              </span>
            ) : (
              <span className="text-green-600 font-semibold">In stock</span>
            )}
          </div>

          {/* Action Buttons */}
          <div className="flex gap-3">
            <Button
              onClick={handleAddToCart}
              disabled={
                !selectedVariant ||
                selectedVariant.stock === 0 ||
                isAddingToCart
              }
              isLoading={isAddingToCart}
              size="lg"
              className="flex-1"
            >
              <ShoppingCart className="w-5 h-5 mr-2" />
              Add to Cart
            </Button>
            <Button variant="outline" size="lg">
              <Heart className="w-5 h-5" />
            </Button>
            <Button variant="outline" size="lg">
              <Share2 className="w-5 h-5" />
            </Button>
          </div>

          {/* Seller Info */}
          <div className="border-t pt-4">
            <div className="text-sm text-gray-600">
              Seller: <span className="font-medium">{product.sellerName}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Tabs: Description, Specs, Reviews */}
      <div className="border-t">
        <div className="flex gap-8 border-b">
          <button
            onClick={() => setActiveTab("description")}
            className={`py-4 px-2 font-medium transition-colors ${
              activeTab === "description"
                ? "text-blue-600 border-b-2 border-blue-600"
                : "text-gray-500 hover:text-gray-700"
            }`}
          >
            Product Description
          </button>
          <button
            onClick={() => setActiveTab("specs")}
            className={`py-4 px-2 font-medium transition-colors ${
              activeTab === "specs"
                ? "text-blue-600 border-b-2 border-blue-600"
                : "text-gray-500 hover:text-gray-700"
            }`}
          >
            Specifications
          </button>
          <button
            onClick={() => setActiveTab("reviews")}
            className={`py-4 px-2 font-medium transition-colors ${
              activeTab === "reviews"
                ? "text-blue-600 border-b-2 border-blue-600"
                : "text-gray-500 hover:text-gray-700"
            }`}
          >
            Reviews ({product?.totalReviews})
          </button>
        </div>

        <div className="py-8">
          {activeTab === "description" && (
            <div className="prose max-w-none">
              <div
                dangerouslySetInnerHTML={{ __html: product.description }}
              />
            </div>
          )}

          {activeTab === "specs" && (
            <div className="space-y-4">
              <table className="w-full">
                <tbody>
                  <tr className="border-b">
                    <td className="py-3 text-gray-600 w-1/3">Brand</td>
                    <td className="py-3 font-medium">{product.brand.name}</td>
                  </tr>
                  <tr className="border-b">
                    <td className="py-3 text-gray-600">Category</td>
                    <td className="py-3 font-medium">{product.category.name}</td>
                  </tr>
                  {product.variants && product.variants.length > 0 && (
                    <tr className="border-b">
                      <td className="py-3 text-gray-600">Variants</td>
                      <td className="py-3 font-medium">
                        {product.variants.map((v) => v.name).join(", ")}
                      </td>
                    </tr>
                  )}
                  <tr className="border-b">
                    <td className="py-3 text-gray-600">Status</td>
                    <td className="py-3 font-medium">
                      {product.totalStock > 0 ? "In stock" : "Out of stock"}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          )}

          {activeTab === "reviews" && (
            <div className="grid md:grid-cols-3 gap-8">
              {/* Left: Rating Summary */}
              <div className="md:col-span-1">
                {ratingSummary && (
                  <ProductRatingSummary
                    summary={ratingSummary}
                    onFilterByRating={(_rating) => {
                      // TODO: Implement rating filter
                    }}
                  />
                )}

                {/* Write Review Button */}
                {isAuthenticated && !showReviewForm && (
                  <button
                    onClick={() => setShowReviewForm(true)}
                    className="mt-6 w-full flex items-center justify-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium transition"
                  >
                    <MessageCircle className="w-5 h-5" />
                    Write a Review
                  </button>
                )}

                {!isAuthenticated && (
                  <div className="mt-6 p-4 bg-gray-50 rounded-lg text-center">
                    <p className="text-sm text-gray-600 mb-3">
                      Sign in to write a review
                    </p>
                    <button
                      onClick={() => navigate("/login", { state: { from: `/products/${id}` } })}
                      className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium transition"
                    >
                      Sign In
                    </button>
                  </div>
                )}
              </div>

              {/* Right: Reviews List */}
              <div className="md:col-span-2">
                {/* Review Form */}
                {showReviewForm && (
                  <div className="mb-6">
                    <ReviewForm
                      productId={id!}
                      onSubmit={async (data) => {
                        try {
                          await createReviewMutation.mutateAsync(data as CreateReviewRequest);
                          setShowReviewForm(false);
                          alert("Review submitted successfully! It will be visible after approval.");
                        } catch (error: any) {
                          alert(error.response?.data?.message || "Failed to submit review");
                        }
                      }}
                      onCancel={() => setShowReviewForm(false)}
                      isSubmitting={createReviewMutation.isPending}
                    />
                  </div>
                )}

                {/* Reviews List */}
                {reviewsData && (
                  <ReviewList
                    reviews={reviewsData.content}
                    isLoading={reviewsLoading}
                    totalReviews={reviewsData.totalElements}
                    onLoadMore={() => setReviewPage((prev) => prev + 1)}
                    hasMore={!reviewsData.last}
                  />
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* AI-Powered Similar Products */}
      <div className="mt-12 py-8 bg-gradient-to-br from-purple-50 to-blue-50 rounded-lg">
        <MLRecommendedProducts
          productId={id}
          type="similar"
          limit={10}
          title="Customers Also Bought"
          description="Frequently bought together - powered by AI collaborative filtering"
        />
      </div>
    </div>
  );
};