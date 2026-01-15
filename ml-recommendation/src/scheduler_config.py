"""
Configuration for Automated ML Training Scheduler

Định nghĩa lịch training và các thông số cho hệ thống tự động học.
"""

import os
from pathlib import Path


# Lịch training (cron format)
TRAINING_SCHEDULES = {
    # Daily incremental training (mỗi ngày lúc 2:00 AM)
    "daily_incremental": {
        "enabled": True,
        "cron": "0 2 * * *",  # 2:00 AM mỗi ngày
        "type": "incremental",
        "description": "Daily incremental learning from new user interactions",
    },
    # Weekly full retraining (Chủ nhật lúc 3:00 AM)
    "weekly_full": {
        "enabled": True,
        "cron": "0 3 * * 0",  # 3:00 AM mỗi Chủ nhật
        "type": "full",
        "description": "Weekly full model retraining with all data",
    },
    # Monthly deep training (Ngày 1 hàng tháng lúc 4:00 AM)
    "monthly_deep": {
        "enabled": True,
        "cron": "0 4 1 * *",  # 4:00 AM ngày 1 mỗi tháng
        "type": "deep",
        "description": "Monthly deep training with hyperparameter optimization",
    },
}



DB_CONFIG = {
    "host": os.getenv("DB_HOST", "localhost"),
    "port": int(os.getenv("DB_PORT", 3306)),
    "database": os.getenv("DB_NAME", "ecommerce"),
    "user": os.getenv("DB_USER", "root"),
    "password": os.getenv("DB_PASSWORD", "12345"),
}

# Connection string
DB_CONNECTION_STRING = (
    f"mysql+pymysql://{DB_CONFIG['user']}:{DB_CONFIG['password']}"
    f"@{DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}"
)


BASE_DIR = Path(__file__).parent.parent
MODELS_DIR = BASE_DIR / "models"
MODELS_ARCHIVE_DIR = MODELS_DIR / "archive"
METRICS_DIR = MODELS_DIR / "metrics"

# Tạo thư mục nếu chưa có
MODELS_ARCHIVE_DIR.mkdir(parents=True, exist_ok=True)
METRICS_DIR.mkdir(parents=True, exist_ok=True)


TRAINING_CONFIG = {
    # Incremental learning config
    "incremental": {
        "lookback_days": 7,  # Lấy data từ 7 ngày gần nhất
        "min_interactions": 10,  # Tối thiểu 10 interactions mới mới training
        "learning_rate": 0.3,  # Tỷ lệ update model (30% new + 70% old)
    },
    # Full retraining config
    "full": {
        "lookback_days": 90,  # Lấy data từ 90 ngày gần nhất
        "min_interactions": 100,
        "test_size": 0.2,  # 20% data để test
    },
    # Deep training config (với hyperparameter tuning)
    "deep": {
        "lookback_days": 180,  # 6 tháng data
        "min_interactions": 500,
        "test_size": 0.2,
        "cv_folds": 5,  # 5-fold cross validation
        "hyperparameter_search": True,
    },
}


EVALUATION_THRESHOLDS = {
    # Precision@10 threshold (tối thiểu để deploy model mới)
    "min_precision_at_10": 0.15,  # 15% precision
    # Recall@10 threshold
    "min_recall_at_10": 0.10,  # 10% recall
    # Category diversity threshold
    "min_category_diversity": 3.0,  # Tối thiểu 3 categories trong top 10
    # Improvement threshold (so với model cũ)
    "min_improvement_percentage": 2.0,  # Cải thiện ít nhất 2%
}


MONITORING_CONFIG = {
    # Log file
    "log_file": BASE_DIR / "logs" / "training_scheduler.log",
    "log_level": "INFO",
    # Metrics tracking
    "track_metrics": True,
    "metrics_retention_days": 365,  # Giữ metrics 1 năm
    # Email alerts (optional)
    "email_alerts_enabled": False,
    "alert_email": "admin@example.com",
    # Slack alerts (optional)
    "slack_alerts_enabled": False,
    "slack_webhook_url": os.getenv("SLACK_WEBHOOK_URL", ""),
}


DEPLOYMENT_CONFIG = {
    # Tự động deploy model mới nếu pass evaluation
    "auto_deploy": True,
    # Backup model cũ trước khi deploy
    "backup_before_deploy": True,
    # Rollback nếu model mới có vấn đề
    "auto_rollback_on_error": True,
    # Restart API service sau khi deploy (cần sudo/systemd)
    "restart_api_service": False,
    "api_service_name": "ml-recommendation-api",
}

RESOURCE_LIMITS = {
    # Max memory usage (GB)
    "max_memory_gb": 8,
    # Max training time (minutes)
    "max_training_time_minutes": 120,
    # Parallel jobs
    "n_jobs": -1,  # Use all CPU cores
}

FEATURE_FLAGS = {
    # Enable A/B testing
    "enable_ab_testing": False,
    # Enable online learning (real-time updates)
    "enable_online_learning": False,
    # Enable model ensemble
    "enable_model_ensemble": False,
    # Enable advanced features (deep learning)
    "enable_advanced_features": False,
}
