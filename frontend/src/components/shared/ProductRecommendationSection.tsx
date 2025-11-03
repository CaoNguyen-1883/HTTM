import { ChevronRight } from "lucide-react";
import { Link } from "react-router-dom";
import { ProductSummary } from "../../lib/types";
import { ProductCard } from "./ProductCard";

interface ProductRecommendationSectionProps {
  title: string;
  description?: string;
  products: ProductSummary[];
  viewAllLink?: string;
}

export const ProductRecommendationSection = ({
  title,
  description,
  products,
  viewAllLink,
}: ProductRecommendationSectionProps) => {
  if (!products || products.length === 0) {
    return null;
  }

  return (
    <section className="py-12">
      <div className="container mx-auto px-4">
        {/* Section Header */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">{title}</h2>
            {description && (
              <p className="text-gray-600 mt-1">{description}</p>
            )}
          </div>
          {viewAllLink && (
            <Link
              to={viewAllLink}
              className="flex items-center gap-2 text-blue-600 hover:text-blue-700 font-medium"
            >
              View All
              <ChevronRight className="w-5 h-5" />
            </Link>
          )}
        </div>

        {/* Products Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {products.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      </div>
    </section>
  );
};
