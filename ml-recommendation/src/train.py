"""
Training script for recommendation model with validation and backup.
Can be run manually or triggered by scheduler.
"""

import pickle
import pandas as pd
import numpy as np
from datetime import datetime
from pathlib import Path
from sqlalchemy import create_engine
from sklearn.metrics.pairwise import cosine_similarity
import logging
import sys
import shutil
import os

# Setup logging
log_dir = Path(__file__).parent.parent / 'logs'
log_dir.mkdir(exist_ok=True)

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s | %(levelname)-8s | %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S',
    handlers=[
        logging.FileHandler(log_dir / 'training.log'),
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)


class ModelTrainer:
    """Handles model training with validation and backup."""

    def __init__(self, db_config=None):
        self.db_config = db_config or self._load_db_config()
        self.model_dir = Path(__file__).parent.parent / 'models'
        self.model_path = self.model_dir / 'item_based_cf.pkl'
        self.backup_dir = self.model_dir / 'backups'
        self.backup_dir.mkdir(exist_ok=True)

        # Training parameters
        self.MIN_CO_OCCURRENCE = 5
        self.PURCHASE_WEIGHT = 1.0
        self.VIEW_WEIGHT = 0.3

    def _load_db_config(self):
        """Load database config from environment or defaults."""
        return {
            'host': os.getenv('DB_HOST', 'localhost'),
            'port': int(os.getenv('DB_PORT', 3306)),
            'database': os.getenv('DB_NAME', 'ecommerce_db'),
            'user': os.getenv('DB_USER', 'root'),
            'password': os.getenv('DB_PASSWORD', 'root')
        }

    def create_db_connection(self):
        """Create database connection."""
        logger.info("Connecting to database...")
        engine = create_engine(
            f"mysql+pymysql://{self.db_config['user']}:{self.db_config['password']}@"
            f"{self.db_config['host']}:{self.db_config['port']}/{self.db_config['database']}"
        )
        return engine

    def backup_current_model(self):
        """Backup current model before training new one."""
        if not self.model_path.exists():
            logger.info("No existing model to backup")
            return None

        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        backup_path = self.backup_dir / f'item_based_cf_{timestamp}.pkl'

        shutil.copy2(self.model_path, backup_path)
        logger.info(f"Backed up current model to {backup_path.name}")

        # Keep only last 5 backups
        backups = sorted(self.backup_dir.glob('*.pkl'), key=lambda p: p.stat().st_mtime)
        if len(backups) > 5:
            for old_backup in backups[:-5]:
                old_backup.unlink()
                logger.info(f"Removed old backup: {old_backup.name}")

        return backup_path

    def load_data(self, engine):
        """Load purchase and view data from database."""
        logger.info("Loading purchase data...")

        query_purchases = """
        SELECT
            HEX(o.user_id) as user_id,
            HEX(pv.product_id) as product_id,
            oi.quantity,
            o.created_at as purchase_date
        FROM orders o
        INNER JOIN order_items oi ON o.id = oi.order_id
        INNER JOIN product_variants pv ON oi.variant_id = pv.id
        WHERE o.order_status IN ('CONFIRMED', 'DELIVERED', 'SHIPPED')
          AND o.is_active = 1
          AND oi.is_active = 1
        ORDER BY o.created_at DESC
        """

        df_purchases = pd.read_sql(query_purchases, engine)
        logger.info(f"Loaded {len(df_purchases):,} purchase records")

        logger.info("Loading view data...")
        query_views = """
        SELECT
            HEX(user_id) as user_id,
            HEX(product_id) as product_id,
            view_count,
            last_viewed_at
        FROM user_product_views
        """

        df_views = pd.read_sql(query_views, engine)
        logger.info(f"Loaded {len(df_views):,} view records")

        return df_purchases, df_views

    def build_interaction_matrix(self, df_purchases, df_views):
        """Build user-item interaction matrix."""
        logger.info("Building interaction matrix...")

        purchase_matrix = df_purchases.groupby(['user_id', 'product_id'])['quantity'].sum()
        purchase_matrix = purchase_matrix * self.PURCHASE_WEIGHT

        view_matrix = df_views.groupby(['user_id', 'product_id'])['view_count'].sum()
        view_matrix = view_matrix * self.VIEW_WEIGHT

        interaction_scores = purchase_matrix.add(view_matrix, fill_value=0)
        user_item_matrix = interaction_scores.unstack(fill_value=0)

        sparsity = 1 - (user_item_matrix > 0).sum().sum() / user_item_matrix.size
        logger.info(f"Matrix shape: {user_item_matrix.shape} (users x products)")
        logger.info(f"Sparsity: {sparsity:.2%}")

        return user_item_matrix

    def calculate_similarity(self, user_item_matrix):
        """Calculate item-item similarity matrix."""
        logger.info("Calculating similarity matrix...")

        item_user_matrix = user_item_matrix.T
        similarity_array = cosine_similarity(item_user_matrix)

        product_ids = item_user_matrix.index.tolist()
        similarity_matrix = pd.DataFrame(
            similarity_array,
            index=product_ids,
            columns=product_ids
        )

        logger.info(f"Similarity matrix shape: {similarity_matrix.shape}")
        return similarity_matrix, product_ids

    def apply_co_occurrence_filter(self, similarity_matrix, user_item_matrix):
        """Apply co-occurrence filtering."""
        logger.info(f"Applying co-occurrence filter (threshold: {self.MIN_CO_OCCURRENCE})...")

        binary_matrix = (user_item_matrix > 0).astype(int)
        co_occurrence = binary_matrix.T @ binary_matrix
        np.fill_diagonal(co_occurrence.values, 0)

        co_occurrence_mask = co_occurrence >= self.MIN_CO_OCCURRENCE
        filtered_similarity = similarity_matrix.copy()
        filtered_similarity = filtered_similarity.where(co_occurrence_mask, 0)
        np.fill_diagonal(filtered_similarity.values, 1.0)

        non_zero_before = (similarity_matrix > 0).sum().sum() // 2
        non_zero_after = (filtered_similarity > 0).sum().sum() // 2
        logger.info(f"Filtered out {non_zero_before - non_zero_after:,} weak correlations")

        return filtered_similarity

    def validate_model(self, model_data):
        """Validate model quality."""
        logger.info("Validating model...")

        metrics = {
            'n_users': model_data['n_users'],
            'n_products': model_data['n_products'],
            'n_interactions': model_data['n_interactions'],
            'sparsity': model_data['sparsity']
        }

        # Basic validation checks
        if metrics['n_products'] < 10:
            raise ValueError(f"Too few products: {metrics['n_products']}")

        if metrics['n_users'] < 50:
            raise ValueError(f"Too few users: {metrics['n_users']}")

        if metrics['sparsity'] > 0.99:
            logger.warning(f"High sparsity: {metrics['sparsity']:.2%}")

        logger.info("Model validation passed")
        logger.info(f"  Users: {metrics['n_users']:,}")
        logger.info(f"  Products: {metrics['n_products']:,}")
        logger.info(f"  Interactions: {metrics['n_interactions']:,}")
        logger.info(f"  Sparsity: {metrics['sparsity']:.2%}")

        return True

    def save_model(self, model_data):
        """Save trained model."""
        logger.info(f"Saving model to {self.model_path.name}...")

        with open(self.model_path, 'wb') as f:
            pickle.dump(model_data, f)

        file_size = self.model_path.stat().st_size / (1024 * 1024)
        logger.info(f"Model saved successfully ({file_size:.2f} MB)")

    def train(self):
        """Main training pipeline."""
        start_time = datetime.now()
        logger.info("=" * 70)
        logger.info("STARTING MODEL TRAINING PIPELINE")
        logger.info("=" * 70)

        try:
            # Backup existing model
            self.backup_current_model()

            # Load data
            engine = self.create_db_connection()
            df_purchases, df_views = self.load_data(engine)

            # Build interaction matrix
            user_item_matrix = self.build_interaction_matrix(df_purchases, df_views)

            # Calculate similarity
            similarity_matrix, product_ids = self.calculate_similarity(user_item_matrix)

            # Apply filtering
            filtered_similarity = self.apply_co_occurrence_filter(
                similarity_matrix, user_item_matrix
            )

            # Prepare model data
            n_interactions = (user_item_matrix > 0).sum().sum()
            sparsity = 1 - (n_interactions / user_item_matrix.size)

            model_data = {
                'similarity_matrix': filtered_similarity,
                'product_ids': product_ids,
                'user_item_matrix': user_item_matrix,
                'trained_at': datetime.now().isoformat(),
                'n_users': len(user_item_matrix),
                'n_products': len(product_ids),
                'n_interactions': int(n_interactions),
                'sparsity': float(sparsity),
                'min_co_occurrence': self.MIN_CO_OCCURRENCE,
                'use_hybrid': True,
                'purchase_weight': self.PURCHASE_WEIGHT,
                'view_weight': self.VIEW_WEIGHT
            }

            # Validate
            self.validate_model(model_data)

            # Save
            self.save_model(model_data)

            # Success
            duration = (datetime.now() - start_time).total_seconds()
            logger.info("=" * 70)
            logger.info(f"TRAINING COMPLETED SUCCESSFULLY in {duration:.1f}s")
            logger.info("=" * 70)

            return True

        except Exception as e:
            logger.error(f"Training failed: {e}", exc_info=True)
            logger.info("=" * 70)
            logger.info("TRAINING FAILED")
            logger.info("=" * 70)
            return False


if __name__ == "__main__":
    trainer = ModelTrainer()
    success = trainer.train()
    sys.exit(0 if success else 1)
