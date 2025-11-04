"""
Model monitoring script.
Tracks model performance and health metrics.
"""

import pickle
import pandas as pd
from pathlib import Path
from datetime import datetime
import json
import logging

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s | %(levelname)-8s | %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S'
)
logger = logging.getLogger(__name__)


class ModelMonitor:
    """Monitors model health and performance."""

    def __init__(self):
        self.model_dir = Path(__file__).parent.parent / 'models'
        self.model_path = self.model_dir / 'item_based_cf.pkl'
        self.metrics_path = self.model_dir / 'metrics.json'
        self.backup_dir = self.model_dir / 'backups'

    def load_model(self):
        """Load current model."""
        if not self.model_path.exists():
            raise FileNotFoundError(f"Model not found: {self.model_path}")

        with open(self.model_path, 'rb') as f:
            model = pickle.load(f)

        return model

    def calculate_metrics(self, model):
        """Calculate current model metrics."""
        metrics = {
            'timestamp': datetime.now().isoformat(),
            'trained_at': model.get('trained_at'),
            'n_users': model.get('n_users'),
            'n_products': model.get('n_products'),
            'n_interactions': model.get('n_interactions'),
            'sparsity': model.get('sparsity'),
            'min_co_occurrence': model.get('min_co_occurrence'),
            'use_hybrid': model.get('use_hybrid'),
            'purchase_weight': model.get('purchase_weight'),
            'view_weight': model.get('view_weight')
        }

        # Calculate additional metrics
        similarity_matrix = model['similarity_matrix']
        non_zero_similarities = (similarity_matrix > 0).sum().sum() // 2
        total_pairs = len(similarity_matrix) * (len(similarity_matrix) - 1) // 2

        metrics['similarity_density'] = non_zero_similarities / total_pairs if total_pairs > 0 else 0
        metrics['avg_similarity'] = float(similarity_matrix[similarity_matrix > 0].mean().mean())

        return metrics

    def get_model_age(self, model):
        """Calculate model age in days."""
        trained_at = datetime.fromisoformat(model['trained_at'])
        age = (datetime.now() - trained_at).total_seconds() / 86400  # Convert to days
        return age

    def check_health(self):
        """Check model health status."""
        logger.info("Checking model health...")

        try:
            model = self.load_model()
            metrics = self.calculate_metrics(model)
            age_days = self.get_model_age(model)

            health_status = {
                'status': 'healthy',
                'issues': [],
                'warnings': [],
                'metrics': metrics,
                'age_days': age_days
            }

            # Check for issues
            if metrics['n_products'] < 10:
                health_status['issues'].append(f"Too few products: {metrics['n_products']}")
                health_status['status'] = 'unhealthy'

            if metrics['n_users'] < 50:
                health_status['issues'].append(f"Too few users: {metrics['n_users']}")
                health_status['status'] = 'unhealthy'

            if metrics['sparsity'] > 0.99:
                health_status['warnings'].append(f"High sparsity: {metrics['sparsity']:.2%}")

            if age_days > 7:
                health_status['warnings'].append(f"Model is {age_days:.1f} days old (consider retraining)")

            return health_status

        except FileNotFoundError:
            return {
                'status': 'error',
                'issues': ['Model file not found'],
                'warnings': [],
                'metrics': {},
                'age_days': None
            }
        except Exception as e:
            return {
                'status': 'error',
                'issues': [f'Error loading model: {str(e)}'],
                'warnings': [],
                'metrics': {},
                'age_days': None
            }

    def save_metrics(self, metrics):
        """Save metrics to file."""
        history = []

        # Load existing history
        if self.metrics_path.exists():
            with open(self.metrics_path, 'r') as f:
                history = json.load(f)

        # Append new metrics
        history.append(metrics)

        # Keep only last 30 entries
        history = history[-30:]

        # Save
        with open(self.metrics_path, 'w') as f:
            json.dump(history, f, indent=2)

        logger.info(f"Metrics saved to {self.metrics_path.name}")

    def display_health_report(self):
        """Display health report."""
        health = self.check_health()

        logger.info("=" * 70)
        logger.info("MODEL HEALTH REPORT")
        logger.info("=" * 70)
        logger.info(f"Status: {health['status'].upper()}")

        if health['age_days']:
            logger.info(f"Model Age: {health['age_days']:.1f} days")

        if health['metrics']:
            logger.info("")
            logger.info("Metrics:")
            logger.info(f"  Users: {health['metrics']['n_users']:,}")
            logger.info(f"  Products: {health['metrics']['n_products']:,}")
            logger.info(f"  Interactions: {health['metrics']['n_interactions']:,}")
            logger.info(f"  Sparsity: {health['metrics']['sparsity']:.2%}")
            logger.info(f"  Similarity Density: {health['metrics']['similarity_density']:.2%}")

        if health['warnings']:
            logger.info("")
            logger.warning("Warnings:")
            for warning in health['warnings']:
                logger.warning(f"  - {warning}")

        if health['issues']:
            logger.info("")
            logger.error("Issues:")
            for issue in health['issues']:
                logger.error(f"  - {issue}")

        logger.info("=" * 70)

        # Save metrics if available
        if health['metrics']:
            self.save_metrics(health['metrics'])

        return health

    def list_backups(self):
        """List available model backups."""
        if not self.backup_dir.exists():
            logger.info("No backups directory found")
            return []

        backups = sorted(self.backup_dir.glob('*.pkl'), key=lambda p: p.stat().st_mtime, reverse=True)

        if not backups:
            logger.info("No backups found")
            return []

        logger.info("=" * 70)
        logger.info("AVAILABLE BACKUPS")
        logger.info("=" * 70)

        for i, backup in enumerate(backups, 1):
            size_mb = backup.stat().st_size / (1024 * 1024)
            modified = datetime.fromtimestamp(backup.stat().st_mtime)
            logger.info(f"{i}. {backup.name}")
            logger.info(f"   Size: {size_mb:.2f} MB")
            logger.info(f"   Date: {modified.strftime('%Y-%m-%d %H:%M:%S')}")

        logger.info("=" * 70)

        return backups


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description='Model Monitor')
    parser.add_argument(
        '--action',
        type=str,
        default='health',
        choices=['health', 'backups'],
        help='Action: health (check model health) or backups (list backups)'
    )

    args = parser.parse_args()

    monitor = ModelMonitor()

    if args.action == 'health':
        monitor.display_health_report()
    elif args.action == 'backups':
        monitor.list_backups()
