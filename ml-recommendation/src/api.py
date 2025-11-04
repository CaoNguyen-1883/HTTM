"""
FastAPI Service for AI-powered Product Recommendations

Endpoints:
- GET /health - Health check
- GET /model/info - Model information
- GET /api/recommendations/similar/{product_id} - Similar products
- GET /api/recommendations/user/{user_id} - Personalized recommendations
- GET /api/recommendations/popular - Popular products (fallback)
- POST /api/model/retrain - Trigger model retraining
"""

from fastapi import FastAPI, HTTPException, Query, Path
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import List, Optional
import logging
from pathlib import Path as PathLib
import sys

from recommender import ItemBasedRecommender

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s | %(name)-12s | %(levelname)-8s | %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S'
)
logger = logging.getLogger(__name__)

# Create FastAPI app
app = FastAPI(
    title="AI Recommendation Service",
    description="Machine Learning-powered product recommendation API",
    version="1.0.0"
)

# Configure CORS for Spring Boot backend
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080", "http://localhost:3000"],  # Spring Boot + React
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global model instance
recommender: Optional[ItemBasedRecommender] = None
MODEL_PATH = PathLib(__file__).parent.parent / 'models' / 'item_based_cf.pkl'


# Pydantic models for request/response
class RecommendationItem(BaseModel):
    product_id: str = Field(..., description="Product UUID")
    score: float = Field(..., description="Recommendation score/similarity")


class RecommendationsResponse(BaseModel):
    recommendations: List[RecommendationItem]
    total: int = Field(..., description="Number of recommendations returned")


class UserRecommendationRequest(BaseModel):
    purchased_products: List[str] = Field(..., description="List of product UUIDs user has purchased")
    exclude_purchased: bool = Field(True, description="Exclude already purchased products")


class HealthResponse(BaseModel):
    status: str
    model_loaded: bool
    message: Optional[str] = None


class ModelInfoResponse(BaseModel):
    status: str
    model_path: str
    metadata: dict
    available_products: int


# Startup event
@app.on_event("startup")
async def startup_event():
    """Load model on startup."""
    global recommender

    logger.info("Initializing AI Recommendation Service...")

    try:
        if MODEL_PATH.exists():
            recommender = ItemBasedRecommender(str(MODEL_PATH))
            logger.info("✓ Model loaded successfully!")
        else:
            logger.warning(f"Model file not found: {MODEL_PATH}")
            logger.warning("Service will start but recommendations will not be available")
    except Exception as e:
        logger.error(f"Failed to load model: {e}", exc_info=True)
        logger.warning("Service will start without model")


# Health check endpoint
@app.get("/health", response_model=HealthResponse, tags=["System"])
async def health_check():
    """
    Health check endpoint.

    Returns service status and model availability.
    """
    return {
        "status": "healthy",
        "model_loaded": recommender is not None,
        "message": "Service is running" if recommender else "Model not loaded"
    }


# Model info endpoint
@app.get("/model/info", response_model=ModelInfoResponse, tags=["System"])
async def get_model_info():
    """
    Get information about the loaded model.

    Returns model metadata, training date, and statistics.
    """
    if not recommender:
        raise HTTPException(status_code=503, detail="Model not loaded")

    return recommender.get_model_info()


# Similar products endpoint
@app.get(
    "/api/recommendations/similar/{product_id}",
    response_model=RecommendationsResponse,
    tags=["Recommendations"]
)
async def get_similar_products(
    product_id: str = Path(..., description="Product UUID (with or without dashes)"),
    limit: int = Query(10, ge=1, le=50, description="Number of recommendations")
):
    """
    Get products similar to the given product.

    Use case: "Customers who bought this also bought..."

    Args:
        product_id: Target product UUID
        limit: Maximum number of recommendations (1-50)

    Returns:
        List of similar products with similarity scores
    """
    if not recommender:
        raise HTTPException(status_code=503, detail="Model not loaded")

    try:
        recommendations = recommender.get_similar_products(product_id, top_k=limit)

        return {
            "recommendations": [
                {
                    "product_id": rec['product_id'],
                    "score": rec['similarity_score']
                }
                for rec in recommendations
            ],
            "total": len(recommendations)
        }

    except Exception as e:
        logger.error(f"Error in get_similar_products: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


# User recommendations endpoint
@app.post(
    "/api/recommendations/user",
    response_model=RecommendationsResponse,
    tags=["Recommendations"]
)
async def get_user_recommendations(
    request: UserRecommendationRequest,
    limit: int = Query(20, ge=1, le=100, description="Number of recommendations")
):
    """
    Get personalized recommendations for a user based on purchase history.

    Use case: "Recommended for you" section

    Args:
        request: User purchase history
        limit: Maximum number of recommendations (1-100)

    Returns:
        List of recommended products with scores
    """
    if not recommender:
        raise HTTPException(status_code=503, detail="Model not loaded")

    if not request.purchased_products:
        raise HTTPException(status_code=400, detail="No purchased products provided")

    try:
        recommendations = recommender.recommend_for_user(
            user_id="api_request",
            purchased_products=request.purchased_products,
            top_k=limit,
            exclude_purchased=request.exclude_purchased
        )

        return {
            "recommendations": recommendations,
            "total": len(recommendations)
        }

    except Exception as e:
        logger.error(f"Error in get_user_recommendations: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


# Alternative GET endpoint for user recommendations (for easier Spring Boot integration)
@app.get(
    "/api/recommendations/user/{user_id}",
    response_model=RecommendationsResponse,
    tags=["Recommendations"]
)
async def get_user_recommendations_by_id(
    user_id: str = Path(..., description="User UUID"),
    purchased_products: str = Query(..., description="Comma-separated list of purchased product UUIDs"),
    limit: int = Query(20, ge=1, le=100, description="Number of recommendations")
):
    """
    Get personalized recommendations for a user (GET version).

    This endpoint is easier to call from Spring Boot using RestTemplate.

    Args:
        user_id: User UUID (for logging)
        purchased_products: Comma-separated product UUIDs
        limit: Maximum number of recommendations

    Returns:
        List of recommended products with scores
    """
    if not recommender:
        raise HTTPException(status_code=503, detail="Model not loaded")

    try:
        # Parse comma-separated product IDs
        product_list = [p.strip() for p in purchased_products.split(',') if p.strip()]

        if not product_list:
            raise HTTPException(status_code=400, detail="No purchased products provided")

        recommendations = recommender.recommend_for_user(
            user_id=user_id,
            purchased_products=product_list,
            top_k=limit,
            exclude_purchased=True
        )

        return {
            "recommendations": recommendations,
            "total": len(recommendations)
        }

    except Exception as e:
        logger.error(f"Error in get_user_recommendations_by_id: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


# Popular products endpoint
@app.get(
    "/api/recommendations/popular",
    response_model=RecommendationsResponse,
    tags=["Recommendations"]
)
async def get_popular_products(
    limit: int = Query(10, ge=1, le=50, description="Number of recommendations")
):
    """
    Get most popular products across all users.

    Use case:
    - Homepage "Trending" or "Best Sellers" section
    - New user cold start (no purchase history)
    - Fallback when personalized recommendations not available

    Args:
        limit: Maximum number of recommendations (1-50)

    Returns:
        List of popular products with popularity scores
    """
    if not recommender:
        raise HTTPException(status_code=503, detail="Model not loaded")

    try:
        recommendations = recommender.get_popular_products(top_k=limit)

        return {
            "recommendations": [
                {
                    "product_id": rec['product_id'],
                    "score": rec['popularity_score']
                }
                for rec in recommendations
            ],
            "total": len(recommendations)
        }

    except Exception as e:
        logger.error(f"Error in get_popular_products: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


# Retrain endpoint (trigger manual retraining)
@app.post("/api/model/retrain", tags=["System"])
async def retrain_model():
    """
    Trigger model retraining.

    This will run the training script and reload the model.
    """
    global recommender

    try:
        logger.info("Starting model retraining...")

        # Import and run training
        import subprocess
        train_script = PathLib(__file__).parent / 'train.py'

        result = subprocess.run(
            [sys.executable, str(train_script)],
            capture_output=True,
            text=True,
            timeout=300  # 5 minutes timeout
        )

        if result.returncode != 0:
            logger.error(f"Training failed: {result.stderr}")
            raise HTTPException(status_code=500, detail="Training failed")

        # Reload model
        recommender = ItemBasedRecommender(str(MODEL_PATH))
        logger.info("✓ Model retrained and reloaded successfully!")

        return {
            "status": "success",
            "message": "Model retrained and reloaded"
        }

    except subprocess.TimeoutExpired:
        raise HTTPException(status_code=504, detail="Training timeout")
    except Exception as e:
        logger.error(f"Error in retrain_model: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


# Root endpoint
@app.get("/", tags=["System"])
async def root():
    """Root endpoint with API information."""
    return {
        "service": "AI Recommendation Service",
        "version": "1.0.0",
        "status": "running",
        "model_loaded": recommender is not None,
        "endpoints": {
            "health": "/health",
            "model_info": "/model/info",
            "similar_products": "/api/recommendations/similar/{product_id}",
            "user_recommendations": "/api/recommendations/user (POST) or /api/recommendations/user/{user_id} (GET)",
            "popular_products": "/api/recommendations/popular",
            "retrain": "/api/model/retrain (POST)"
        },
        "docs": "/docs"
    }


if __name__ == "__main__":
    import uvicorn

    logger.info("Starting AI Recommendation API Server...")
    logger.info(f"Model path: {MODEL_PATH}")

    # Disable reload on Windows to avoid MemoryError
    uvicorn.run(
        app,  # Pass app object directly instead of string
        host="0.0.0.0",
        port=8000,
        reload=False,  # Disabled for Windows compatibility
        log_level="info"
    )
