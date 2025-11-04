"""
Recommender class for loading and serving trained ML model.
"""

import pickle
import numpy as np
import pandas as pd
from pathlib import Path
from typing import List, Dict, Optional
import logging

logger = logging.getLogger(__name__)


def hex_to_uuid(hex_string: str) -> str:
    """
    Convert hex string to UUID format.

    Args:
        hex_string: Hex string (e.g., 'BA6F86D76CA44499AEE9108F60A9A476')

    Returns:
        UUID format string (e.g., 'ba6f86d7-6ca4-4499-aee9-108f60a9a476')

    Example:
        >>> hex_to_uuid('BA6F86D76CA44499AEE9108F60A9A476')
        'ba6f86d7-6ca4-4499-aee9-108f60a9a476'
    """
    # Remove any existing dashes and convert to lowercase
    hex_clean = hex_string.replace('-', '').lower()

    # Insert dashes at UUID positions: 8, 12, 16, 20
    if len(hex_clean) == 32:
        return f"{hex_clean[:8]}-{hex_clean[8:12]}-{hex_clean[12:16]}-{hex_clean[16:20]}-{hex_clean[20:]}"

    # Return as-is if not valid length
    return hex_string.lower()


class ItemBasedRecommender:
    """
    Item-Based Collaborative Filtering Recommender.

    Loads trained model and provides recommendation methods.
    """

    def __init__(self, model_path: str):
        """
        Initialize recommender by loading trained model.

        Args:
            model_path: Path to pickled model file
        """
        self.model_path = Path(model_path)
        self.model = None
        self.similarity_matrix = None
        self.product_ids = None
        self.user_item_matrix = None
        self.metadata = {}

        self.load_model()

    def load_model(self):
        """Load trained model from pickle file."""
        logger.info(f"Loading model from {self.model_path}")

        if not self.model_path.exists():
            raise FileNotFoundError(f"Model file not found: {self.model_path}")

        with open(self.model_path, 'rb') as f:
            self.model = pickle.load(f)

        # Extract model components
        self.similarity_matrix = self.model['similarity_matrix']
        self.product_ids = self.model['product_ids']
        self.user_item_matrix = self.model.get('user_item_matrix')

        # Store metadata
        self.metadata = {
            'trained_at': self.model.get('trained_at'),
            'n_users': self.model.get('n_users'),
            'n_products': self.model.get('n_products'),
            'n_interactions': self.model.get('n_interactions'),
            'sparsity': self.model.get('sparsity'),
            'min_co_occurrence': self.model.get('min_co_occurrence'),
            'use_hybrid': self.model.get('use_hybrid', False),
            'purchase_weight': self.model.get('purchase_weight', 1.0),
            'view_weight': self.model.get('view_weight', 0.3)
        }

        logger.info("Model loaded successfully")
        logger.info(f"Products: {self.metadata['n_products']}")
        logger.info(f"Users: {self.metadata['n_users']}")
        logger.info(f"Sparsity: {self.metadata['sparsity']:.2%}")

    def get_similar_products(self, product_id: str, top_k: int = 10) -> List[Dict]:
        """
        Get products most similar to the given product.

        Args:
            product_id: Target product ID (UUID without dashes)
            top_k: Number of recommendations to return

        Returns:
            List of dicts with keys: product_id, similarity_score
        """
        # Normalize product ID (remove dashes, uppercase)
        product_id = product_id.replace('-', '').upper()

        if product_id not in self.similarity_matrix.index:
            logger.warning(f"Product {product_id} not found in model")
            return []

        # Get similarity scores
        similarities = self.similarity_matrix.loc[product_id]

        # Remove the product itself
        similarities = similarities.drop(product_id, errors='ignore')

        # Filter out zero similarities
        similarities = similarities[similarities > 0]

        # Sort and get top K
        top_items = similarities.nlargest(top_k)

        # Format results with UUID conversion
        recommendations = [
            {
                'product_id': hex_to_uuid(prod_id),
                'similarity_score': float(score)
            }
            for prod_id, score in top_items.items()
        ]

        logger.info(f"Generated {len(recommendations)} recommendations for product {product_id}")
        return recommendations

    def recommend_for_user(self, user_id: str, purchased_products: List[str],
                          top_k: int = 20, exclude_purchased: bool = True) -> List[Dict]:
        """
        Generate personalized recommendations for a user based on purchase history.

        IMPROVED: Uses WEIGHTED SCORING instead of simple average.
        Products purchased more frequently get higher weight in recommendations.

        Args:
            user_id: User ID (for logging)
            purchased_products: List of product IDs user has purchased (can include duplicates)
            top_k: Number of recommendations
            exclude_purchased: Whether to exclude already purchased products

        Returns:
            List of dicts with keys: product_id, score, recommendation_type

        Algorithm:
            1. Count purchase frequency for each product (duplicates = higher weight)
            2. For each purchased product:
               - Get similar products from similarity matrix
               - Weight similarity by purchase frequency
            3. Aggregate weighted scores
            4. Return top K candidates
            5. Fallback to popular products if no personalized recs available
        """
        # Normalize product IDs
        purchased_products = [p.replace('-', '').upper() for p in purchased_products]

        # Count purchase frequency (duplicates = user likes it more)
        from collections import Counter
        product_counts = Counter(purchased_products)

        # Filter to products that exist in model
        valid_products = {p: count for p, count in product_counts.items()
                         if p in self.similarity_matrix.index}

        if not valid_products:
            logger.warning(f"No valid products found for user {user_id}, using popular fallback")
            return self.get_popular_products(top_k)

        # IMPROVED: Weighted scoring based on purchase frequency
        candidate_scores = {}

        for product_id, purchase_count in valid_products.items():
            # Get similarity scores for this product
            item_similarities = self.similarity_matrix.loc[product_id]

            # For each similar item
            for candidate_id, similarity in item_similarities.items():
                # Skip if already purchased or similarity is 0
                if candidate_id in valid_products or similarity == 0:
                    continue

                # Accumulate weighted score: purchase_count * similarity
                if candidate_id not in candidate_scores:
                    candidate_scores[candidate_id] = 0
                candidate_scores[candidate_id] += purchase_count * similarity

        # If no candidates found, fallback to popular products
        if not candidate_scores:
            logger.info(f"No personalized recommendations for user {user_id}, using popular fallback")
            return self.get_popular_products(top_k, exclude_products=list(valid_products.keys()))

        # Sort by score and get top K
        top_items = sorted(candidate_scores.items(), key=lambda x: x[1], reverse=True)[:top_k]

        # Format results with UUID conversion
        recommendations = [
            {
                'product_id': hex_to_uuid(prod_id),
                'score': float(score),
                'recommendation_type': 'personalized'
            }
            for prod_id, score in top_items
        ]

        logger.info(f"Generated {len(recommendations)} PERSONALIZED recommendations for user {user_id} "
                   f"based on {len(valid_products)} unique products ({sum(valid_products.values())} total purchases)")
        return recommendations

    def get_model_info(self) -> Dict:
        """Get model metadata and statistics."""
        return {
            'status': 'loaded',
            'model_path': str(self.model_path),
            'metadata': self.metadata,
            'available_products': len(self.product_ids)
        }

    def is_product_available(self, product_id: str) -> bool:
        """Check if product exists in model."""
        product_id = product_id.replace('-', '').upper()
        return product_id in self.similarity_matrix.index

    def get_all_products(self) -> List[str]:
        """Get list of all product IDs in model."""
        return self.product_ids

    def get_popular_products(self, top_k: int = 10, exclude_products: Optional[List[str]] = None) -> List[Dict]:
        """
        Get most popular products based on total interactions across all users.

        Use case: Homepage recommendations, new user cold start, fallback strategy.

        Args:
            top_k: Number of recommendations to return
            exclude_products: List of product IDs to exclude (e.g., already purchased)

        Returns:
            List of dicts with keys: product_id, popularity_score, recommendation_type

        Example:
            >>> recommender.get_popular_products(top_k=5)
            [
                {'product_id': 'PROD123', 'popularity_score': 1234.5, 'recommendation_type': 'popular'},
                ...
            ]
        """
        if self.user_item_matrix is None:
            logger.warning("User-item matrix not available, cannot compute popular products")
            return []

        # Calculate popularity: sum of all user interactions per product
        popularity_scores = self.user_item_matrix.sum(axis=0).sort_values(ascending=False)

        # Normalize exclude_products
        if exclude_products:
            exclude_products = [p.replace('-', '').upper() for p in exclude_products]
            popularity_scores = popularity_scores.drop(exclude_products, errors='ignore')

        # Get top K
        top_products = popularity_scores.head(top_k)

        # Format results with UUID conversion
        recommendations = [
            {
                'product_id': hex_to_uuid(prod_id),
                'popularity_score': float(score),
                'recommendation_type': 'popular'
            }
            for prod_id, score in top_products.items()
        ]

        logger.info(f"Generated {len(recommendations)} popular product recommendations")
        return recommendations
