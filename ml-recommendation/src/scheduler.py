"""
Automated training scheduler.
Runs model retraining at specified intervals.
"""

from apscheduler.schedulers.blocking import BlockingScheduler
from apscheduler.triggers.cron import CronTrigger
import logging
import sys
from pathlib import Path
from datetime import datetime
import requests

from train import ModelTrainer

# Setup logging
log_dir = Path(__file__).parent.parent / 'logs'
log_dir.mkdir(exist_ok=True)

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s | %(levelname)-8s | %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S',
    handlers=[
        logging.FileHandler(log_dir / 'scheduler.log'),
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)


class RecommendationScheduler:
    """Schedules automated model retraining."""

    def __init__(self):
        self.scheduler = BlockingScheduler()
        self.api_url = "http://localhost:8000"

    def reload_api_model(self):
        """Notify API to reload model after training."""
        try:
            response = requests.post(f"{self.api_url}/api/model/retrain")
            if response.status_code == 200:
                logger.info("API model reloaded successfully")
            else:
                logger.warning(f"API reload returned status {response.status_code}")
        except requests.exceptions.ConnectionError:
            logger.warning("Could not connect to API server (it may be offline)")
        except Exception as e:
            logger.error(f"Error reloading API model: {e}")

    def train_job(self):
        """Training job to be scheduled."""
        logger.info("=" * 70)
        logger.info("SCHEDULED TRAINING JOB STARTED")
        logger.info(f"Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        logger.info("=" * 70)

        try:
            # Run training
            trainer = ModelTrainer()
            success = trainer.train()

            if success:
                logger.info("Training job completed successfully")
                # Reload API model if training succeeded
                self.reload_api_model()
            else:
                logger.error("Training job failed")

        except Exception as e:
            logger.error(f"Training job error: {e}", exc_info=True)

        logger.info("=" * 70)
        logger.info("SCHEDULED TRAINING JOB FINISHED")
        logger.info("=" * 70)

    def start(self, schedule_type="daily"):
        """
        Start the scheduler.

        Args:
            schedule_type: "daily", "weekly", "custom"
        """
        logger.info("=" * 70)
        logger.info("RECOMMENDATION MODEL SCHEDULER")
        logger.info("=" * 70)

        if schedule_type == "daily":
            # Run every day at 2 AM
            self.scheduler.add_job(
                self.train_job,
                CronTrigger(hour=2, minute=0),
                id='daily_training',
                name='Daily Model Training',
                replace_existing=True
            )
            logger.info("Schedule: Daily at 2:00 AM")

        elif schedule_type == "weekly":
            # Run every Sunday at 2 AM
            self.scheduler.add_job(
                self.train_job,
                CronTrigger(day_of_week='sun', hour=2, minute=0),
                id='weekly_training',
                name='Weekly Model Training',
                replace_existing=True
            )
            logger.info("Schedule: Weekly on Sunday at 2:00 AM")

        elif schedule_type == "custom":
            # Example: Run every 6 hours
            self.scheduler.add_job(
                self.train_job,
                CronTrigger(hour='*/6'),
                id='custom_training',
                name='Custom Model Training',
                replace_existing=True
            )
            logger.info("Schedule: Every 6 hours")

        logger.info("Scheduler started. Press Ctrl+C to stop.")
        logger.info("=" * 70)

        try:
            self.scheduler.start()
        except (KeyboardInterrupt, SystemExit):
            logger.info("Scheduler stopped by user")
            self.scheduler.shutdown()


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description='Recommendation Model Scheduler')
    parser.add_argument(
        '--schedule',
        type=str,
        default='daily',
        choices=['daily', 'weekly', 'custom'],
        help='Schedule type: daily (2 AM), weekly (Sunday 2 AM), or custom (every 6h)'
    )

    args = parser.parse_args()

    scheduler = RecommendationScheduler()
    scheduler.start(schedule_type=args.schedule)
