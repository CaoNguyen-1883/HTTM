"""
Automated Training Scheduler

Lập lịch tự động training models theo cấu hình:
- Daily: Incremental learning
- Weekly: Full retraining
- Monthly: Deep training với hyperparameter tuning
"""

import logging
import sys
from datetime import datetime
from pathlib import Path

from apscheduler.schedulers.blocking import BlockingScheduler
from apscheduler.triggers.cron import CronTrigger
from model_deployer import ModelDeployer
from model_trainer import ModelTrainer
from performance_tracker import PerformanceTracker
from scheduler_config import (
    MONITORING_CONFIG,
    TRAINING_SCHEDULES,
)

# Setup logging
log_dir = Path(__file__).parent.parent / "logs"
log_dir.mkdir(exist_ok=True)

logging.basicConfig(
    level=getattr(logging, MONITORING_CONFIG["log_level"]),
    format="%(asctime)s | %(name)-20s | %(levelname)-8s | %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
    handlers=[
        logging.FileHandler(MONITORING_CONFIG["log_file"]),
        logging.StreamHandler(sys.stdout),
    ],
)
logger = logging.getLogger(__name__)


class TrainingScheduler:
    """
    Automated ML Training Scheduler với APScheduler.
    """

    def __init__(self):
        """Initialize scheduler."""
        self.scheduler = BlockingScheduler()
        self.trainer = ModelTrainer()
        self.deployer = ModelDeployer()
        self.tracker = PerformanceTracker()

        logger.info("=" * 80)
        logger.info("Training Scheduler Initialized")
        logger.info("=" * 80)

    def setup_schedules(self):
        """Setup training schedules based on configuration."""
        logger.info("Setting up training schedules...")

        # Daily incremental training
        if TRAINING_SCHEDULES["daily_incremental"]["enabled"]:
            cron = TRAINING_SCHEDULES["daily_incremental"]["cron"]
            self.scheduler.add_job(
                self.run_incremental_training,
                trigger=CronTrigger.from_crontab(cron),
                id="daily_incremental",
                name="Daily Incremental Training",
                max_instances=1,
                coalesce=True,
            )
            logger.info(f"✓ Scheduled daily incremental training: {cron}")

        # Weekly full retraining
        if TRAINING_SCHEDULES["weekly_full"]["enabled"]:
            cron = TRAINING_SCHEDULES["weekly_full"]["cron"]
            self.scheduler.add_job(
                self.run_full_training,
                trigger=CronTrigger.from_crontab(cron),
                id="weekly_full",
                name="Weekly Full Retraining",
                max_instances=1,
                coalesce=True,
            )
            logger.info(f"✓ Scheduled weekly full training: {cron}")

        # Monthly deep training
        if TRAINING_SCHEDULES["monthly_deep"]["enabled"]:
            cron = TRAINING_SCHEDULES["monthly_deep"]["cron"]
            self.scheduler.add_job(
                self.run_deep_training,
                trigger=CronTrigger.from_crontab(cron),
                id="monthly_deep",
                name="Monthly Deep Training",
                max_instances=1,
                coalesce=True,
            )
            logger.info(f"✓ Scheduled monthly deep training: {cron}")

        logger.info("All schedules configured successfully")

    def run_incremental_training(self):
        """Execute incremental training job."""
        logger.info("=" * 80)
        logger.info("INCREMENTAL TRAINING JOB STARTED")
        logger.info("=" * 80)

        try:
            # Train model
            results = self.trainer.train_incremental()

            if results["status"] == "success":
                # Track performance
                self.tracker.log_training_run(results)

                # Evaluate and deploy if improved
                should_deploy = self.deployer.evaluate_for_deployment(
                    results["version"], results["metrics"]
                )

                if should_deploy:
                    logger.info("Model passed evaluation, deploying...")
                    self.deployer.deploy_model(
                        results["version"], training_type="incremental"
                    )
                else:
                    logger.warning("Model did not meet deployment criteria")

            logger.info("Incremental training job completed successfully")

        except Exception as e:
            logger.error(f"Incremental training failed: {e}", exc_info=True)
            self._send_alert(f"Incremental training failed: {e}")

        logger.info("=" * 80)

    def run_full_training(self):
        """Execute full retraining job."""
        logger.info("=" * 80)
        logger.info("FULL RETRAINING JOB STARTED")
        logger.info("=" * 80)

        try:
            # Train model
            results = self.trainer.train_full()

            if results["status"] == "success":
                # Track performance
                self.tracker.log_training_run(results)

                # Evaluate and deploy
                should_deploy = self.deployer.evaluate_for_deployment(
                    results["version"], results["metrics"]
                )

                if should_deploy:
                    logger.info("Model passed evaluation, deploying...")
                    self.deployer.deploy_model(results["version"], training_type="full")
                else:
                    logger.warning("Model did not meet deployment criteria")

            logger.info("Full training job completed successfully")

        except Exception as e:
            logger.error(f"Full training failed: {e}", exc_info=True)
            self._send_alert(f"Full training failed: {e}")

        logger.info("=" * 80)

    def run_deep_training(self):
        """Execute deep training with hyperparameter tuning."""
        logger.info("=" * 80)
        logger.info("DEEP TRAINING JOB STARTED")
        logger.info("=" * 80)

        try:
            # Train model
            results = self.trainer.train_deep()

            if results["status"] == "success":
                # Track performance
                self.tracker.log_training_run(results)

                # Evaluate and deploy
                should_deploy = self.deployer.evaluate_for_deployment(
                    results["version"], results["metrics"]
                )

                if should_deploy:
                    logger.info("Model passed evaluation, deploying...")
                    self.deployer.deploy_model(results["version"], training_type="deep")
                else:
                    logger.warning("Model did not meet deployment criteria")

            logger.info("Deep training job completed successfully")

        except Exception as e:
            logger.error(f"Deep training failed: {e}", exc_info=True)
            self._send_alert(f"Deep training failed: {e}")

        logger.info("=" * 80)

    def run_manual_training(self, training_type: str = "full"):
        """
        Run manual training (for testing).

        Args:
            training_type: Type of training (incremental/full/deep)
        """
        logger.info(f"Starting manual {training_type} training...")

        if training_type == "incremental":
            self.run_incremental_training()
        elif training_type == "full":
            self.run_full_training()
        elif training_type == "deep":
            self.run_deep_training()
        else:
            logger.error(f"Unknown training type: {training_type}")

    def start(self):
        """Start the scheduler."""
        logger.info("=" * 80)
        logger.info("Starting Training Scheduler")
        logger.info("=" * 80)

        self.setup_schedules()

        # Print scheduled jobs
        logger.info("\nScheduled Jobs:")
        for job in self.scheduler.get_jobs():
            logger.info(f"  - {job.name}: {job.trigger}")

        logger.info("\nScheduler is now running. Press Ctrl+C to exit.")
        logger.info("=" * 80)

        try:
            self.scheduler.start()
        except (KeyboardInterrupt, SystemExit):
            logger.info("Scheduler stopped by user")
            self.cleanup()

    def cleanup(self):
        """Clean up resources."""
        logger.info("Cleaning up resources...")
        self.trainer.close()
        self.tracker.close()
        logger.info("Cleanup completed")

    def _send_alert(self, message: str):
        """
        Send alert notification (email/Slack).

        Args:
            message: Alert message
        """
        logger.warning(f"ALERT: {message}")

        # TODO: Implement email/Slack alerts
        # if MONITORING_CONFIG["email_alerts_enabled"]:
        #     send_email_alert(message)
        #
        # if MONITORING_CONFIG["slack_alerts_enabled"]:
        #     send_slack_alert(message)


def main():
    """Main entry point."""
    scheduler = TrainingScheduler()

    # Check for manual training mode
    if len(sys.argv) > 1:
        training_type = sys.argv[1]
        logger.info(f"Running in MANUAL mode: {training_type}")
        scheduler.run_manual_training(training_type)
    else:
        # Start automated scheduler
        scheduler.start()


if __name__ == "__main__":
    main()
