import { Link } from "react-router-dom";
import { ArrowRight, ShoppingBag, TrendingUp, Star, Clock } from "lucide-react";
import { useHomePageRecommendations } from "../../lib/hooks";
import { ProductRecommendationSection } from "../../components/shared/ProductRecommendationSection";
import { AIRecommendedProducts } from "../../components/customer/AIRecommendedProducts";
import { useAuthStore } from "../../lib/stores/authStore";

export const HomePage = () => {
  const { data: recommendations, isLoading } = useHomePageRecommendations();
  const { user } = useAuthStore();

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Hero Section */}
      <section className="bg-gradient-to-r from-blue-600 to-blue-800 text-white">
        <div className="container mx-auto px-4 py-20">
          <div className="max-w-3xl">
            <h1 className="text-5xl font-bold mb-6">
              Welcome to E-Commerce
            </h1>
            <p className="text-xl mb-8 text-blue-100">
              Discover the best deals on gaming gear, PC components, and accessories
            </p>
            <div className="flex gap-4">
              <Link
                to="/products"
                className="flex items-center gap-2 bg-white text-blue-600 px-6 py-3 rounded-lg font-semibold hover:bg-blue-50 transition-colors"
              >
                Shop Now
                <ArrowRight className="w-5 h-5" />
              </Link>
              <Link
                to="/products?sort=createdAt,desc"
                className="flex items-center gap-2 bg-blue-700 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-600 transition-colors border border-blue-500"
              >
                New Arrivals
                <Clock className="w-5 h-5" />
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-12 border-b">
        <div className="container mx-auto px-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="flex items-start gap-4">
              <div className="bg-blue-100 p-3 rounded-lg">
                <ShoppingBag className="w-6 h-6 text-blue-600" />
              </div>
              <div>
                <h3 className="font-bold text-lg mb-2">Wide Selection</h3>
                <p className="text-gray-600">
                  Browse thousands of products from top brands
                </p>
              </div>
            </div>
            <div className="flex items-start gap-4">
              <div className="bg-green-100 p-3 rounded-lg">
                <TrendingUp className="w-6 h-6 text-green-600" />
              </div>
              <div>
                <h3 className="font-bold text-lg mb-2">Best Prices</h3>
                <p className="text-gray-600">
                  Competitive pricing on all products
                </p>
              </div>
            </div>
            <div className="flex items-start gap-4">
              <div className="bg-yellow-100 p-3 rounded-lg">
                <Star className="w-6 h-6 text-yellow-600" />
              </div>
              <div>
                <h3 className="font-bold text-lg mb-2">Top Rated</h3>
                <p className="text-gray-600">
                  Quality products with verified reviews
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* AI-Powered Personalized Recommendations */}
      <div className="py-8 bg-gradient-to-br from-purple-50 to-blue-50">
        <div className="container mx-auto px-4">
          {user ? (
            <AIRecommendedProducts
              type="personalized"
              limit={10}
              title="Dành Cho Bạn"
              description="Gợi ý cá nhân hóa bằng AI - dựa trên lịch sử mua hàng của bạn"
              showAIBadge={true}
            />
          ) : (
            <AIRecommendedProducts
              type="popular"
              limit={10}
              title="Sản Phẩm Phổ Biến"
              description="Những sản phẩm được yêu thích nhất"
              showAIBadge={true}
            />
          )}
        </div>
      </div>

      {/* Loading State */}
      {isLoading && (
        <div className="py-20">
          <div className="container mx-auto px-4">
            <div className="flex items-center justify-center">
              <div className="text-gray-500">Loading recommendations...</div>
            </div>
          </div>
        </div>
      )}

      {/* Recommendation Sections */}
      {!isLoading && recommendations && (
        <div className="bg-white">
          {recommendations.map((section, index) => (
            <div
              key={index}
              className={index % 2 === 0 ? "bg-white" : "bg-gray-50"}
            >
              <ProductRecommendationSection
                title={section.sectionTitle}
                description={section.sectionDescription}
                products={section.products}
                viewAllLink={getViewAllLink(section.type)}
              />
            </div>
          ))}
        </div>
      )}

      {/* Call to Action */}
      <section className="bg-gradient-to-r from-gray-900 to-gray-800 text-white py-16">
        <div className="container mx-auto px-4 text-center">
          <h2 className="text-3xl font-bold mb-4">
            Ready to Start Shopping?
          </h2>
          <p className="text-gray-300 mb-8 max-w-2xl mx-auto">
            Join thousands of satisfied customers and discover amazing deals on the latest products
          </p>
          <Link
            to="/products"
            className="inline-flex items-center gap-2 bg-blue-600 text-white px-8 py-3 rounded-lg font-semibold hover:bg-blue-700 transition-colors"
          >
            Browse All Products
            <ArrowRight className="w-5 h-5" />
          </Link>
        </div>
      </section>
    </div>
  );
};

// Helper function to get view all link based on recommendation type
function getViewAllLink(type: string): string {
  const linkMap: Record<string, string> = {
    TRENDING: "/products?sort=viewCount,desc",
    BEST_SELLERS: "/products?sort=purchaseCount,desc",
    TOP_RATED: "/products?sort=averageRating,desc",
    NEW_ARRIVALS: "/products?sort=createdAt,desc",
  };

  return linkMap[type] || "/products";
}
