import { productsApi } from './products.api';
import {
  getUserRecommendations,
  getSimilarProducts,
  getPopularProducts,
  RecommendationItem,
} from './recommendations.api';
import { ProductSummary, Product } from '../types';

/**
 * Convert full Product to ProductSummary for list display
 */
const productToSummary = (product: Product): ProductSummary => {
  const minPrice = Math.min(...product.variants.map(v => v.price));
  const maxPrice = Math.max(...product.variants.map(v => v.price));
  const primaryImage = product.images.find(img => img.isPrimary)?.imageUrl || product.images[0]?.imageUrl || '';

  return {
    id: product.id,
    name: product.name,
    slug: product.slug,
    shortDescription: product.shortDescription,
    categoryName: product.category.name,
    brandName: product.brand.name,
    basePrice: product.basePrice,
    minPrice,
    maxPrice,
    status: product.status,
    primaryImage,
    averageRating: product.averageRating,
    totalReviews: product.totalReviews,
    totalStock: product.totalStock,
    hasStock: product.totalStock > 0,
    defaultVariantId: product.variants.find(v => v.isDefault)?.id || product.variants[0]?.id,
  };
};

/**
 * Get AI recommendations và enrich với product details từ Spring Boot
 */
export const enrichedRecommendationsApi = {

  /**
   * Get personalized recommendations với full product details
   * Use case: Homepage "Recommended for You"
   */
  getPersonalizedProducts: async (
    userId: string,
    purchasedProductIds: string[],
    limit: number = 20
  ): Promise<ProductSummary[]> => {
    try {
      if (purchasedProductIds.length === 0) {
        // New user → show popular products
        console.log('No purchase history, using popular products');
        return enrichedRecommendationsApi.getPopularProductsWithDetails(limit);
      }

      // Step 1: Get AI recommendations (chỉ có product IDs + scores)
      const aiResponse = await getUserRecommendations(userId, purchasedProductIds, limit);

      if (aiResponse.recommendations.length === 0) {
        console.log('No personalized recommendations, using popular products');
        return enrichedRecommendationsApi.getPopularProductsWithDetails(limit);
      }

      // Step 2: Get full product details from Spring Boot
      const productIds = aiResponse.recommendations.map(rec => rec.product_id);
      const products = await enrichedRecommendationsApi.getProductsByIds(productIds);

      // Step 3: Sort by AI score (maintain recommendation order)
      const sortedProducts = productIds
        .map(id => products.find(p => p.id === id))
        .filter(p => p !== undefined) as ProductSummary[];

      return sortedProducts;

    } catch (error) {
      console.error('Error getting personalized products:', error);
      // Fallback to popular products
      return enrichedRecommendationsApi.getPopularProductsWithDetails(limit);
    }
  },

  /**
   * Get similar products với full details
   * Use case: Product Detail Page "Customers Also Bought"
   */
  getSimilarProductsWithDetails: async (
    productId: string,
    limit: number = 10
  ): Promise<ProductSummary[]> => {
    try {
      // Step 1: Get AI recommendations
      const aiResponse = await getSimilarProducts(productId, limit);

      if (aiResponse.recommendations.length === 0) {
        return [];
      }

      // Step 2: Get full product details
      const productIds = aiResponse.recommendations.map(rec => rec.product_id);
      const products = await enrichedRecommendationsApi.getProductsByIds(productIds);

      // Step 3: Sort by similarity score
      const sortedProducts = productIds
        .map(id => products.find(p => p.id === id))
        .filter(p => p !== undefined) as ProductSummary[];

      return sortedProducts;

    } catch (error) {
      console.error('Error getting similar products:', error);
      return [];
    }
  },

  /**
   * Get popular products với full details
   * Use case: Homepage "Best Sellers", New Users
   */
  getPopularProductsWithDetails: async (
    limit: number = 20
  ): Promise<ProductSummary[]> => {
    try {
      // Step 1: Get AI recommendations
      const aiResponse = await getPopularProducts(limit);

      if (aiResponse.recommendations.length === 0) {
        // Fallback to Spring Boot best sellers
        console.log('No popular products from AI, using Spring Boot best sellers');
        const response = await productsApi.getBestSellers({ size: limit });
        return response.content;
      }

      // Step 2: Get full product details
      const productIds = aiResponse.recommendations.map((rec: RecommendationItem) => rec.product_id);
      const products = await enrichedRecommendationsApi.getProductsByIds(productIds);

      // Step 3: Sort by popularity score
      const sortedProducts = productIds
        .map((id: string) => products.find((p: ProductSummary) => p.id === id))
        .filter((p: ProductSummary | undefined): p is ProductSummary => p !== undefined);

      return sortedProducts;

    } catch (error) {
      console.error('Error getting popular products:', error);
      // Final fallback: Spring Boot best sellers
      const response = await productsApi.getBestSellers({ size: limit });
      return response.content;
    }
  },

  /**
   * Helper: Get multiple products by IDs from Spring Boot
   */
  getProductsByIds: async (productIds: string[]): Promise<ProductSummary[]> => {
    try {
      // Call từng product (chắc chắn work với API hiện tại)
      const products = await Promise.all(
        productIds.map(id =>
          productsApi.getProductById(id).catch(err => {
            console.error(`Error fetching product ${id}:`, err);
            return null;
          })
        )
      );

      // Filter nulls and convert to ProductSummary
      return products
        .filter((p): p is Product => p !== null)
        .map(productToSummary);

    } catch (error) {
      console.error('Error fetching products by IDs:', error);
      return [];
    }
  }
};
