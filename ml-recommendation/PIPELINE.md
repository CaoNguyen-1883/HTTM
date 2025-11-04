# Automated Training Pipeline

Pipeline tự động để hệ thống recommendation model có thể học và cải thiện theo thời gian.

## Tính năng

- **Automated Training**: Tự động train lại model theo lịch định kỳ
- **Model Validation**: Kiểm tra chất lượng model trước khi deploy
- **Backup Management**: Tự động backup model cũ trước khi train
- **Health Monitoring**: Theo dõi health và performance của model
- **Auto Reload**: Tự động reload API sau khi train xong

## Cấu trúc

```
ml-recommendation/
├── src/
│   ├── train.py          # Training script với validation
│   ├── scheduler.py      # Automated scheduler
│   ├── monitor.py        # Health monitoring
│   └── api.py           # API server
│
├── models/
│   ├── item_based_cf.pkl       # Current model
│   ├── metrics.json            # Historical metrics
│   └── backups/                # Model backups
│       ├── item_based_cf_20250105_020000.pkl
│       └── item_based_cf_20250104_020000.pkl
│
└── logs/
    ├── training.log      # Training logs
    └── scheduler.log     # Scheduler logs
```

## Cài đặt

### Bước 1: Cài dependencies

```bash
pip install -r requirements.txt
```

Các package mới được thêm:
- `APScheduler`: Automated task scheduling

### Bước 2: Kiểm tra model hiện tại

```bash
cd src
python monitor.py --action health
```

Output:
```
======================================================================
MODEL HEALTH REPORT
======================================================================
Status: HEALTHY
Model Age: 0.5 days

Metrics:
  Users: 102
  Products: 33
  Interactions: 3,331
  Sparsity: 1.04%
  Similarity Density: 103.03%
======================================================================
```

## Sử dụng

### Manual Training

Chạy training thủ công:

```bash
cd src
python train.py
```

Features:
- Tự động backup model cũ
- Validation model mới
- Logging chi tiết
- Exit code 0 (success) hoặc 1 (failure)

Output:
```
======================================================================
STARTING MODEL TRAINING PIPELINE
======================================================================
Backed up current model to item_based_cf_20250105_143022.pkl
Connecting to database...
Loaded 8,014 purchase records
Loaded 3,173 view records
Building interaction matrix...
Matrix shape: (102, 33) (users x products)
Sparsity: 1.04%
Calculating similarity matrix...
Applying co-occurrence filter (threshold: 5)...
Validating model...
Model validation passed
  Users: 102
  Products: 33
  Interactions: 3,331
  Sparsity: 1.04%
Saving model to item_based_cf.pkl...
Model saved successfully (0.04 MB)
======================================================================
TRAINING COMPLETED SUCCESSFULLY in 3.2s
======================================================================
```

### Automated Scheduling

#### Option 1: Daily Training (2 AM)

```bash
cd src
python scheduler.py --schedule daily
```

Model sẽ tự động train mỗi ngày lúc 2:00 AM.

#### Option 2: Weekly Training (Sunday 2 AM)

```bash
cd src
python scheduler.py --schedule weekly
```

Model sẽ tự động train mỗi Chủ nhật lúc 2:00 AM.

#### Option 3: Custom Schedule (Every 6 hours)

```bash
cd src
python scheduler.py --schedule custom
```

Model sẽ tự động train mỗi 6 giờ.

### Monitoring

#### Check Model Health

```bash
python monitor.py --action health
```

Kiểm tra:
- Model age (số ngày kể từ lần train cuối)
- Metrics (users, products, interactions, sparsity)
- Warnings (high sparsity, old model)
- Issues (too few users/products)

#### List Backups

```bash
python monitor.py --action backups
```

Output:
```
======================================================================
AVAILABLE BACKUPS
======================================================================
1. item_based_cf_20250105_020000.pkl
   Size: 0.04 MB
   Date: 2025-01-05 02:00:00

2. item_based_cf_20250104_020000.pkl
   Size: 0.04 MB
   Date: 2025-01-04 02:00:00
======================================================================
```

## Configuration

### Environment Variables

Tạo file `.env` trong folder `ml-recommendation`:

```env
# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ecommerce_db
DB_USER=root
DB_PASSWORD=root
```

### Training Parameters

Edit `src/train.py`:

```python
class ModelTrainer:
    def __init__(self, db_config=None):
        # Training parameters
        self.MIN_CO_OCCURRENCE = 5      # Minimum co-occurrence threshold
        self.PURCHASE_WEIGHT = 1.0      # Weight for purchase data
        self.VIEW_WEIGHT = 0.3          # Weight for view data
```

## Production Deployment

### Option 1: systemd Service (Linux)

Tạo file `/etc/systemd/system/recommendation-scheduler.service`:

```ini
[Unit]
Description=Recommendation Model Scheduler
After=network.target

[Service]
Type=simple
User=your-user
WorkingDirectory=/path/to/ml-recommendation/src
ExecStart=/usr/bin/python3 scheduler.py --schedule daily
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Start service:
```bash
sudo systemctl enable recommendation-scheduler
sudo systemctl start recommendation-scheduler
sudo systemctl status recommendation-scheduler
```

### Option 2: Cron Job (Linux/Mac)

Edit crontab:
```bash
crontab -e
```

Add:
```bash
# Train model daily at 2 AM
0 2 * * * cd /path/to/ml-recommendation/src && /usr/bin/python3 train.py >> /path/to/logs/cron.log 2>&1
```

### Option 3: Windows Task Scheduler

1. Open Task Scheduler
2. Create Basic Task
3. Name: "Recommendation Model Training"
4. Trigger: Daily at 2:00 AM
5. Action: Start a program
   - Program: `python`
   - Arguments: `train.py`
   - Start in: `D:\work-space\HTTM\ml-recommendation\src`

## Logs

### Training Log

Location: `logs/training.log`

```
2025-01-05 02:00:00 | INFO     | ======================================================================
2025-01-05 02:00:00 | INFO     | STARTING MODEL TRAINING PIPELINE
2025-01-05 02:00:00 | INFO     | ======================================================================
2025-01-05 02:00:01 | INFO     | Backed up current model to item_based_cf_20250105_020000.pkl
...
2025-01-05 02:00:03 | INFO     | TRAINING COMPLETED SUCCESSFULLY in 3.2s
```

### Scheduler Log

Location: `logs/scheduler.log`

```
2025-01-05 00:00:00 | INFO     | ======================================================================
2025-01-05 00:00:00 | INFO     | RECOMMENDATION MODEL SCHEDULER
2025-01-05 00:00:00 | INFO     | ======================================================================
2025-01-05 00:00:00 | INFO     | Schedule: Daily at 2:00 AM
2025-01-05 00:00:00 | INFO     | Scheduler started. Press Ctrl+C to stop.
```

## Metrics Tracking

File `models/metrics.json` lưu historical metrics:

```json
[
  {
    "timestamp": "2025-01-05T02:00:00",
    "trained_at": "2025-01-05T02:00:00",
    "n_users": 102,
    "n_products": 33,
    "n_interactions": 3331,
    "sparsity": 0.0104,
    "similarity_density": 1.0303,
    "avg_similarity": 0.7654
  }
]
```

## Best Practices

### Training Frequency

- **E-commerce nhỏ** (< 1000 users): Weekly
- **E-commerce trung bình** (1000-10000 users): Daily
- **E-commerce lớn** (> 10000 users): Every 6 hours

### Backup Retention

Pipeline tự động giữ 5 backups gần nhất. Adjust trong `train.py`:

```python
# Keep only last 5 backups
if len(backups) > 5:
    for old_backup in backups[:-5]:
        old_backup.unlink()
```

### Validation Thresholds

```python
def validate_model(self, model_data):
    if metrics['n_products'] < 10:
        raise ValueError("Too few products")

    if metrics['n_users'] < 50:
        raise ValueError("Too few users")

    if metrics['sparsity'] > 0.99:
        logger.warning("High sparsity")
```

## Troubleshooting

### Training Fails

Check logs:
```bash
tail -f logs/training.log
```

Common issues:
- Database connection failed
- Insufficient data (< 50 users, < 10 products)
- High sparsity (> 99%)

### Scheduler Not Running

Check if scheduler is running:
```bash
ps aux | grep scheduler.py
```

Restart scheduler:
```bash
pkill -f scheduler.py
python scheduler.py --schedule daily &
```

### API Not Reloading

Sau khi train, API cần reload model. Có 2 cách:

Manual:
```bash
curl -X POST http://localhost:8000/api/model/retrain
```

Automatic (scheduler tự động gọi sau khi train xong).

## Advanced Features

### Custom Training Logic

Extend `ModelTrainer` class:

```python
class CustomTrainer(ModelTrainer):
    def custom_preprocessing(self, df):
        # Add custom preprocessing
        pass

    def train(self):
        # Override training logic
        pass
```

### Integration với Monitoring Tools

Send metrics to Prometheus/Grafana:

```python
from prometheus_client import Gauge

model_age_gauge = Gauge('model_age_days', 'Model age in days')
model_age_gauge.set(age_days)
```

## Summary

Pipeline này giúp hệ thống recommendation tự động học và cải thiện theo thời gian:

1. **Automated Training**: Model tự động train theo lịch
2. **Quality Assurance**: Validation đảm bảo model mới tốt
3. **Safety**: Backup model cũ trước khi thay thế
4. **Monitoring**: Track health và performance
5. **Auto Reload**: API tự động reload model mới

Hệ thống sẽ ngày càng thông minh hơn khi có thêm dữ liệu từ users!
