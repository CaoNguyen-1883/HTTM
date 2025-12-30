"""
FastAPI Service v2 for Hybrid Recommendation System

New Endpoints:
- GET /api/v2/recommendations/for-you - Hybrid (70% CF + 30% CB) for homepage
- GET /api/v2/recommendations/similar/{product_id} - Content-Based for similar products
- GET /api/v2/recommendations/cross-sell/{product_id} - CF for "Also Bought"
- GET /health - Health check
- GET /model/info - Model information
"""

import logging
import sys
from pathlib import Path as PathLib
from typing import List, Optional

from fastapi import FastAPI, HTTPException, Path, Query
from fastapi.middleware.cors import CORSMiddleware
from hybrid_recommender import HybridRecommender
from pydantic import BaseModel, Field

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(name)-12s | %(levelname)-8s | %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
logger = logging.getLogger(__name__)

# Create FastAPI app
app = FastAPI(
    title="AI Recommendation Service v2 (Hybrid)",
    description="Hybrid ML-powered recommendations: CF + CB",
    version="2.0.0",
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080", "http://localhost:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global model instance
recommender: Optional[HybridRecommender] = None
MODELS_DIR = PathLib(__file__).parent.parent / "models"


# Pydantic models
class RecommendationItem(BaseModel):
    product_id: str = Field(..., description="Product UUID")
    score: float = Field(..., description="Recommendation score")
    recommendation_type: Optional[str] = Field(
        None, description="Type: hybrid/content_based/collaborative"
    )


class RecommendationsResponse(BaseModel):
    recommendations: List[RecommendationItem]
    total: int = Field(..., description="Number of recommendations")
    source: Optional[str] = Field(None, description="Model source: hybrid")


class HealthResponse(BaseModel):
    status: str
    model_loaded: bool
    model_type: Optional[str] = None
    message: Optional[str] = None


class ModelInfoResponse(BaseModel):
    status: str
    model_type: str
    models_dir: str
    metadata: dict


# Startup event
@app.on_event("startup")
async def startup_event():
    """Load hybrid model on startup."""
    global recommender

    logger.info("Initializing Hybrid Recommendation Service v2...")

    try:
        if (MODELS_DIR / "cf_model.pkl").exists() and (
            MODELS_DIR / "cb_model.pkl"
        ).exists():
            recommender = HybridRecommender(str(MODELS_DIR))
            logger.info("Hybrid model loaded successfully!")
        else:
            logger.warning(f"Hybrid models not found in {MODELS_DIR}")
            logger.warning(
                "Service will start but recommendations will not be available"
            )
    except Exception as e:
        logger.error(f"Failed to load hybrid model: {e}", exc_info=True)
        logger.warning("Service will start without model")


# Health check
@app.get("/health", response_model=HealthResponse, tags=["System"])
async def health_check():
    """Health check endpoint."""
    return {
        "status": "healthy",
        "model_loaded": recommender is not None,
        "model_type": "hybrid" if recommender else None,
        "message": "Hybrid model loaded" if recommender else "Model not loaded",
    }


# Model info
@app.get("/model/info", response_model=ModelInfoResponse, tags=["System"])
async def get_model_info():
    """Get hybrid model information."""
    if not recommender:
        raise HTTPException(status_code=503, detail="Model not loaded")

    return {
        "status": "loaded",
        "model_type": "hybrid",
        "models_dir": str(MODELS_DIR),
        "metadata": recommender.get_model_info(),
    }


# PRIORITY #1: Hybrid "For You" recommendations
@app.get(
    "/api/v2/recommendations/for-you",
    response_model=RecommendationsResponse,
    tags=["Recommendations"],
)
async def get_for_you_recommendations(
    user_id: str = Query(..., description="User UUID"),
    viewed_products: str = Query(
        ..., description="Comma-separated viewed product UUIDs"
    ),
    limit: int = Query(10, ge=1, le=50, description="Number of recommendations"),
):
    """
    Get HYBRID personalized recommendations (70% CF + 30% CB).

    Use case: Homepage "For You" section

    Algorithm:
    1. Get CF scores from user's interaction history
    2. Get CB scores from product features
    3. Normalize both to [0, 1]
    4. Combine: 70% CF + 30% CB

    Args:
        user_id: User UUID
        viewed_products: Comma-separated product UUIDs
        limit: Number of recommendations (1-50)

    Returns:
        List of hybrid recommendations with scores
    """
    if not recommender:
        raise HTTPException(status_code=503, detail="Hybrid model not loaded")

    try:
        # Parse product IDs
        product_list = [p.strip() for p in viewed_products.split(",") if p.strip()]

        if not product_list:
            raise HTTPException(status_code=400, detail="No viewed products provided")

        # Get hybrid recommendations
        recommendations = recommender.get_hybrid_recommendations(
            user_id=user_id, viewed_products=product_list, top_k=limit
        )

        return {
            "recommendations": recommendations,
            "total": len(recommendations),
            "source": "hybrid",
        }

    except Exception as e:
        logger.error(f"Error in get_for_you_recommendations: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


# PRIORITY #2: Content-Based similar products
@app.get(
    "/api/v2/recommendations/similar/{product_id}",
    response_model=RecommendationsResponse,
    tags=["Recommendations"],
)
async def get_similar_products(
    product_id: str = Path(..., description="Product UUID"),
    limit: int = Query(10, ge=1, le=50, description="Number of recommendations"),
):
    """
    Get products similar based on CONTENT (tags, category, brand, name).

    Use case: Product detail page "Similar Products" section

    Algorithm:
    - Weighted features: Tags 50%, Category 20%, Brand 20%, Name 10%
    - TF-IDF for text features
    - Cosine similarity

    Args:
        product_id: Target product UUID
        limit: Number of recommendations (1-50)

    Returns:
        List of content-based similar products
    """
    if not recommender:
        raise HTTPException(status_code=503, detail="Hybrid model not loaded")

    try:
        recommendations = recommender.get_similar_products(product_id, top_k=limit)

        return {
            "recommendations": recommendations,
            "total": len(recommendations),
            "source": "hybrid_cb",
        }

    except Exception as e:
        logger.error(f"Error in get_similar_products: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


# PRIORITY #3: Collaborative cross-sell
@app.get(
    "/api/v2/recommendations/cross-sell/{product_id}",
    response_model=RecommendationsResponse,
    tags=["Recommendations"],
)
async def get_cross_sell_recommendations(
    product_id: str = Path(..., description="Product UUID"),
    limit: int = Query(10, ge=1, le=50, description="Number of recommendations"),
):
    """
    Get products that users who viewed this also viewed (CF).

    Use case: Product detail page "Customers Also Bought" section

    Algorithm:
    - User interaction patterns
    - Item-item collaborative filtering
    - "Users who viewed X also viewed Y"

    Args:
        product_id: Target product UUID
        limit: Number of recommendations (1-50)

    Returns:
        List of collaborative cross-sell recommendations
    """
    if not recommender:
        raise HTTPException(status_code=503, detail="Hybrid model not loaded")

    try:
        recommendations = recommender.get_cross_sell_recommendations(
            product_id, top_k=limit
        )

        return {
            "recommendations": recommendations,
            "total": len(recommendations),
            "source": "hybrid_cf",
        }

    except Exception as e:
        logger.error(f"Error in get_cross_sell_recommendations: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


# Root endpoint
@app.get("/", tags=["System"])
async def root():
    """Root endpoint with API information."""
    return {
        "service": "AI Recommendation Service v2 (Hybrid)",
        "version": "2.0.0",
        "status": "running",
        "model_loaded": recommender is not None,
        "recommendation_strategies": {
            "hybrid": "70% CF + 30% CB - Homepage 'For You'",
            "content_based": "Product features - 'Similar Products'",
            "collaborative": "User patterns - 'Customers Also Bought'",
        },
        "endpoints": {
            "health": "GET /health",
            "model_info": "GET /model/info",
            "for_you": "GET /api/v2/recommendations/for-you?user_id=X&viewed_products=A,B,C",
            "similar": "GET /api/v2/recommendations/similar/{product_id}",
            "cross_sell": "GET /api/v2/recommendations/cross-sell/{product_id}",
        },
        "docs": "/docs",
    }


if __name__ == "__main__":
    import uvicorn

    logger.info("Starting Hybrid Recommendation API v2...")
    logger.info(f"Models directory: {MODELS_DIR}")

    uvicorn.run(
        app,
        host="0.0.0.0",
        port=8001,  # Different port from v1 (8000)
        reload=False,
        log_level="info",
    )
