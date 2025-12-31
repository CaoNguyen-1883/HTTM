"""
Hybrid Recommender class combining CF and CB filtering.

Implements three recommendation strategies:
1. Hybrid (70% CF + 30% CB) - For personalized homepage "For You" section
2. Content-Based - For "Similar Products" on product detail pages
3. Collaborative Filtering - For "Customers Also Bought" cross-sell
"""

import logging
import pickle
from pathlib import Path
from typing import Dict, List, Optional

import numpy as np
import pandas as pd

logger = logging.getLogger(__name__)


def hex_to_uuid(hex_string: str) -> str:
    """
    Convert hex string to UUID format.

    Args:
        hex_string: Hex string (e.g., 'BA6F86D76CA44499AEE9108F60A9A476')

    Returns:
        UUID format string (e.g., 'ba6f86d7-6ca4-4499-aee9-108f60a9a476')
    """
    hex_clean = hex_string.replace("-", "").lower()
    if len(hex_clean) == 32:
        return f"{hex_clean[:8]}-{hex_clean[8:12]}-{hex_clean[12:16]}-{hex_clean[16:20]}-{hex_clean[20:]}"
    return hex_string.lower()


def uuid_to_hex(uuid_string: str) -> str:
    """
    Convert UUID format to hex string (for model lookup).

    Args:
        uuid_string: UUID format (e.g., 'ba6f86d7-6ca4-4499-aee9-108f60a9a476')

    Returns:
        Hex string uppercase (e.g., 'BA6F86D76CA44499AEE9108F60A9A476')
    """
    return uuid_string.replace("-", "").upper()


def normalize_scores(scores_dict: Dict[str, float]) -> Dict[str, float]:
    """
    Min-Max normalization to scale scores to [0, 1].

    Args:
        scores_dict: Dictionary of {product_id: score}

    Returns:
        Normalized scores in [0, 1] range
    """
    if not scores_dict:
        return {}

    scores_array = np.array(list(scores_dict.values()))
    min_score = scores_array.min()
    max_score = scores_array.max()

    if max_score == min_score:
        return {k: 1.0 for k in scores_dict}

    return {
        k: (v - min_score) / (max_score - min_score) for k, v in scores_dict.items()
    }


class HybridRecommender:
    """
    Hybrid Recommendation System combining CF and CB filtering.

    Model Architecture:
    - CF Model: Item-Based Collaborative Filtering (interaction patterns)
    - CB Model: Content-Based Filtering (product features: tags, category, brand, name)
    - Hybrid: 70% CF + 30% CB with Min-Max normalization

    Use Cases:
    1. get_hybrid_recommendations() → Homepage "For You" section
    2. get_similar_products() → Product detail page "Similar Products"
    3. get_cross_sell_recommendations() → Product detail page "Customers Also Bought"
    """

    def __init__(self, models_dir: str = "models"):
        """
        Initialize hybrid recommender by loading all models.

        Args:
            models_dir: Directory containing model files
        """
        self.models_dir = Path(models_dir)
        self.cf_model = None
        self.cb_model = None
        self.hybrid_cache = None
        self.metadata = {}

        self.load_models()

    def load_models(self):
        """Load CF, CB, and Hybrid models from disk."""
        logger.info(f"Loading models from {self.models_dir}")

        # Load CF model
        cf_path = self.models_dir / "cf_model.pkl"
        if not cf_path.exists():
            raise FileNotFoundError(f"CF model not found: {cf_path}")

        with open(cf_path, "rb") as f:
            self.cf_model = pickle.load(f)
        logger.info("✓ Loaded CF model")

        # Load CB model
        cb_path = self.models_dir / "cb_model.pkl"
        if not cb_path.exists():
            raise FileNotFoundError(f"CB model not found: {cb_path}")

        with open(cb_path, "rb") as f:
            self.cb_model = pickle.load(f)
        logger.info("✓ Loaded CB model")

        # Load Hybrid cache (pre-computed recommendations)
        hybrid_path = self.models_dir / "hybrid_model.pkl"
        if hybrid_path.exists():
            with open(hybrid_path, "rb") as f:
                self.hybrid_cache = pickle.load(f)
            logger.info(f"✓ Loaded Hybrid cache for {len(self.hybrid_cache)} users")
        else:
            logger.warning("Hybrid cache not found, will compute on-the-fly")
            self.hybrid_cache = {}

        # Load metadata
        metadata_path = self.models_dir / "metadata.json"
        if metadata_path.exists():
            import json

            with open(metadata_path, "r") as f:
                self.metadata = json.load(f)
            logger.info("✓ Loaded metadata")

        logger.info("All models loaded successfully")

    def get_hybrid_recommendations(
        self,
        user_id: str,
        viewed_products: List[str],
        top_k: int = 10,
        use_cache: bool = True,
    ) -> List[Dict]:
        """
        Get hybrid recommendations (70% CF + 30% CB) for a user.

        Use Case: Homepage "For You" personalized section

        Args:
            user_id: User ID
            viewed_products: List of product IDs user has viewed/purchased
            top_k: Number of recommendations to return
            use_cache: Whether to use pre-computed cache (faster)

        Returns:
            List of dicts with keys: product_id, score, recommendation_type

        Example:
            >>> recommender.get_hybrid_recommendations(
            ...     user_id="user123",
            ...     viewed_products=["prod1", "prod2"],
            ...     top_k=10
            ... )
        """
        # Normalize user_id
        user_id_clean = user_id.replace("-", "").upper()

        # Try cache first
        if use_cache and user_id_clean in self.hybrid_cache:
            cached_recs = self.hybrid_cache[user_id_clean][:top_k]
            logger.info(f"Using cached hybrid recommendations for user {user_id}")
            return [
                {
                    "product_id": hex_to_uuid(prod_id),
                    "score": float(score),
                    "recommendation_type": "hybrid",
                }
                for prod_id, score in cached_recs
            ]

        # Compute on-the-fly
        logger.info(f"Computing hybrid recommendations for user {user_id}")

        # Normalize product IDs
        viewed_products = [p.replace("-", "").upper() for p in viewed_products]

        # Get CF scores
        cf_scores = self._get_cf_scores(viewed_products)

        # Get CB scores
        cb_scores = self._get_cb_scores(viewed_products)

        # Normalize both score sets
        cf_scores_norm = normalize_scores(cf_scores)
        cb_scores_norm = normalize_scores(cb_scores)

        # Combine with weights (70% CF + 30% CB)
        hybrid_scores = {}
        all_products = set(list(cf_scores_norm.keys()) + list(cb_scores_norm.keys()))

        for product_id in all_products:
            cf_score = cf_scores_norm.get(product_id, 0)
            cb_score = cb_scores_norm.get(product_id, 0)
            # Adjusted weights: 40% CF + 60% CB (more content-based)
            log.info(f"CF score: {cf_score}, CB score: {cb_score}")
            hybrid_scores[product_id] = (cf_score * 0.1) + (cb_score * 0.9)

        # Sort and return top K
        top_items = sorted(hybrid_scores.items(), key=lambda x: x[1], reverse=True)[
            :top_k
        ]

        recommendations = [
            {
                "product_id": hex_to_uuid(prod_id),
                "score": float(score),
                "recommendation_type": "hybrid",
            }
            for prod_id, score in top_items
        ]

        logger.info(
            f"Generated {len(recommendations)} hybrid recommendations for user {user_id}"
        )
        return recommendations

    def get_similar_products(self, product_id: str, top_k: int = 10) -> List[Dict]:
        """
        Get products similar based on CONTENT (tags, category, brand, name).

        Use Case: Product detail page "Similar Products" section

        Args:
            product_id: Target product ID
            top_k: Number of recommendations

        Returns:
            List of dicts with keys: product_id, similarity_score, recommendation_type
        """
        # Normalize product ID
        product_id = product_id.replace("-", "").upper()

        if product_id not in self.cb_model["product_similarity"].index:
            logger.warning(f"Product {product_id} not found in CB model")
            return []

        # Get similarity scores from CB model
        similarities = self.cb_model["product_similarity"].loc[product_id]

        # Remove the product itself
        similarities = similarities.drop(product_id, errors="ignore")

        # Filter out zero similarities
        similarities = similarities[similarities > 0]

        # Sort and get top K
        top_items = similarities.nlargest(top_k)

        recommendations = [
            {
                "product_id": hex_to_uuid(prod_id),
                "score": float(score),
                "recommendation_type": "content_based",
            }
            for prod_id, score in top_items.items()
        ]

        logger.info(
            f"Generated {len(recommendations)} CB recommendations for product {product_id}"
        )
        return recommendations

    def get_cross_sell_recommendations(
        self, product_id: str, top_k: int = 10
    ) -> List[Dict]:
        """
        Get products that users who viewed this product also viewed (CF).

        Use Case: Product detail page "Customers Also Bought" section

        Args:
            product_id: Target product ID
            top_k: Number of recommendations

        Returns:
            List of dicts with keys: product_id, similarity_score, recommendation_type
        """
        # Normalize product ID
        product_id = product_id.replace("-", "").upper()

        if product_id not in self.cf_model["item_similarity"].index:
            logger.warning(f"Product {product_id} not found in CF model")
            return []

        # Get similarity scores from CF model
        similarities = self.cf_model["item_similarity"].loc[product_id]

        # Remove the product itself
        similarities = similarities.drop(product_id, errors="ignore")

        # Filter out zero similarities
        similarities = similarities[similarities > 0]

        # Sort and get top K
        top_items = similarities.nlargest(top_k)

        recommendations = [
            {
                "product_id": hex_to_uuid(prod_id),
                "score": float(score),
                "recommendation_type": "collaborative",
            }
            for prod_id, score in top_items.items()
        ]

        logger.info(
            f"Generated {len(recommendations)} CF cross-sell recommendations for product {product_id}"
        )
        return recommendations

    def _get_cf_scores(self, viewed_products: List[str]) -> Dict[str, float]:
        """
        Get CF scores by aggregating similarity from viewed products.

        Args:
            viewed_products: List of product IDs

        Returns:
            Dictionary of {product_id: cf_score}
        """
        cf_scores = {}

        for product_id in viewed_products:
            if product_id in self.cf_model["item_similarity"].index:
                similar_items = self.cf_model["item_similarity"][product_id].to_dict()
                for item_id, similarity in similar_items.items():
                    if item_id not in viewed_products:  # Exclude already viewed
                        cf_scores[item_id] = cf_scores.get(item_id, 0) + similarity

        return cf_scores

    def _get_cb_scores(self, viewed_products: List[str]) -> Dict[str, float]:
        """
        Get CB scores by aggregating similarity from viewed products.

        Args:
            viewed_products: List of product IDs

        Returns:
            Dictionary of {product_id: cb_score}
        """
        cb_scores = {}

        for product_id in viewed_products:
            if product_id in self.cb_model["product_similarity"].index:
                similar_items = self.cb_model["product_similarity"][
                    product_id
                ].to_dict()
                for item_id, similarity in similar_items.items():
                    if item_id not in viewed_products:  # Exclude already viewed
                        cb_scores[item_id] = cb_scores.get(item_id, 0) + similarity

        return cb_scores

    def get_model_info(self) -> Dict:
        """Get model metadata and statistics."""
        return {
            "status": "loaded",
            "models_dir": str(self.models_dir),
            "metadata": self.metadata,
            "cf_products": len(self.cf_model["product_ids"]),
            "cb_products": len(self.cb_model["product_ids"]),
            "cached_users": len(self.hybrid_cache),
        }
