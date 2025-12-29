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
      <section className="relative bg-white overflow-hidden">
        {/* Decorative Background Elements */}
        <div className="absolute -top-40 -right-40 w-96 h-96 bg-blue-50 rounded-full blur-3xl opacity-30 pointer-events-none" />
        <div className="absolute top-20 right-1/4 w-2 h-2 bg-blue-600 rounded-full opacity-60" />
        <div className="absolute left-0 top-0 h-1 w-20 bg-blue-600" />

        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <div className="max-w-7xl mx-auto py-16 sm:py-20 lg:py-24">
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 lg:gap-16 items-center">
              {/* Left Column - Content */}
              <div className="lg:col-span-7">
                {/* Key Phrase */}
                <div className="text-sm font-semibold text-blue-600 uppercase tracking-wider mb-4">
                  Gaming Gear Redefined
                </div>

                {/* Heading */}
                <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold text-gray-900 leading-tight tracking-tight max-w-2xl mb-6">
                  Premium PC Components for Every Build
                </h1>

                {/* Subheadline */}
                <p className="text-lg sm:text-xl text-gray-600 leading-relaxed max-w-xl mb-10">
                  Discover cutting-edge hardware, peripherals, and accessories
                  trusted by gamers and creators worldwide.
                </p>

                {/* CTAs */}
                <div className="flex flex-col sm:flex-row gap-4">
                  <Link
                    to="/products"
                    className="group inline-flex items-center justify-center gap-3 px-8 py-4 bg-gray-900 text-white rounded-xl font-semibold text-base shadow-lg shadow-gray-900/25 hover:bg-gray-800 hover:shadow-xl hover:scale-105 transition-all duration-200"
                  >
                    Shop Now
                    <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
                  </Link>

                  <Link
                    to="/products?sort=createdAt,desc"
                    className="inline-flex items-center justify-center gap-3 px-8 py-4 bg-white text-gray-900 border-2 border-gray-200 rounded-xl font-semibold text-base hover:border-gray-900 hover:bg-gray-50 transition-all duration-200"
                  >
                    New Arrivals
                    <Clock className="w-5 h-5" />
                  </Link>
                </div>
              </div>

              {/* Right Column - Geometric Element */}
              <div className="hidden lg:block lg:col-span-5">
                <div className="grid grid-cols-4 gap-3 opacity-60">
                  {/* Row 1 */}
                  <div className="aspect-square bg-blue-600 opacity-10 rounded-lg" />
                  <div className="aspect-square bg-blue-600 opacity-100 rounded-lg shadow-lg" />
                  <div className="aspect-square bg-gray-200 rounded-lg" />
                  <div className="aspect-square bg-blue-600 opacity-20 rounded-lg" />

                  {/* Row 2 */}
                  <div className="aspect-square bg-gray-200 rounded-lg" />
                  <div className="aspect-square bg-blue-600 opacity-30 rounded-lg" />
                  <div className="aspect-square bg-blue-600 opacity-10 rounded-lg" />
                  <div className="aspect-square bg-blue-600 opacity-100 rounded-lg shadow-lg" />

                  {/* Row 3 */}
                  <div className="aspect-square bg-blue-600 opacity-100 rounded-lg shadow-lg" />
                  <div className="aspect-square bg-blue-600 opacity-10 rounded-lg" />
                  <div className="aspect-square bg-gray-200 rounded-lg" />
                  <div className="aspect-square bg-blue-600 opacity-20 rounded-lg" />

                  {/* Row 4 */}
                  <div className="aspect-square bg-blue-600 opacity-20 rounded-lg" />
                  <div className="aspect-square bg-gray-200 rounded-lg" />
                  <div className="aspect-square bg-blue-600 opacity-30 rounded-lg" />
                  <div className="aspect-square bg-blue-600 opacity-10 rounded-lg" />
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Bottom Divider */}
        <div className="absolute bottom-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-gray-200 to-transparent" />
      </section>

      {/* Features Section */}
      {/*<section className="py-12 border-b">
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
      </section>*/}

      {/* AI-Powered Personalized Recommendations */}
      <div className="py-8 bg-gradient-to-br from-purple-50 to-blue-50">
        <div className="container mx-auto px-4">
          {user ? (
            <AIRecommendedProducts
              type="personalized"
              limit={10}
              title="For You"
              description="Recommended based on your browsing history"
              showAIBadge={false}
            />
          ) : (
            <AIRecommendedProducts
              type="popular"
              limit={10}
              title="Popular Products"
              description="Popular products across all categories"
              showAIBadge={false}
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
          <h2 className="text-3xl font-bold mb-4">Ready to Start Shopping?</h2>
          <p className="text-gray-300 mb-8 max-w-2xl mx-auto">
            Join thousands of satisfied customers and discover amazing deals on
            the latest products
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
