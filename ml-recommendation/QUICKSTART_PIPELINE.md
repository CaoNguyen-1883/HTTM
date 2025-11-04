# Pipeline Quick Start Guide

## Cài đặt

```bash
# 1. Cài dependencies
pip install -r requirements.txt

# 2. Kiểm tra model hiện tại
python src/monitor.py --action health
```

## Các lệnh chính

### Training

```bash
# Manual training (chạy ngay)
python src/train.py

# Automated training - Daily at 2 AM
python src/scheduler.py --schedule daily

# Automated training - Weekly (Sunday 2 AM)
python src/scheduler.py --schedule weekly

# Automated training - Every 6 hours
python src/scheduler.py --schedule custom
```

### Monitoring

```bash
# Check model health
python src/monitor.py --action health

# List backups
python src/monitor.py --action backups

# View logs
tail -f logs/training.log
tail -f logs/scheduler.log
```

### API Operations

```bash
# Start API
python src/api.py

# Trigger retrain via API
curl -X POST http://localhost:8000/api/model/retrain

# Check API health
curl http://localhost:8000/health

# View model info
curl http://localhost:8000/model/info
```

## Logs và Metrics

**Logs:**
- `logs/training.log` - Training logs
- `logs/scheduler.log` - Scheduler logs

**Metrics:**
- `models/metrics.json` - Historical metrics
- `models/backups/` - Model backups (keeps last 5)

## Production Setup

**Windows Task Scheduler:**
1. Open Task Scheduler
2. Create Task
3. Trigger: Daily at 2:00 AM
4. Action: `python D:\work-space\HTTM\ml-recommendation\src\train.py`

**Linux Cron:**
```bash
# Add to crontab -e
0 2 * * * cd /path/to/ml-recommendation/src && python train.py
```

**systemd Service:**
```bash
# Create /etc/systemd/system/rec-scheduler.service
sudo systemctl enable rec-scheduler
sudo systemctl start rec-scheduler
```

## Troubleshooting

**Training fails:**
```bash
# Check logs
tail -f logs/training.log

# Test database connection
python -c "from train import ModelTrainer; ModelTrainer().create_db_connection()"

# Check model validation
python src/monitor.py --action health
```

**API not reloading:**
```bash
# Manual reload
curl -X POST http://localhost:8000/api/model/retrain

# Restart API
pkill -f api.py
python src/api.py &
```

**Scheduler not running:**
```bash
# Check process
ps aux | grep scheduler

# View logs
tail -f logs/scheduler.log

# Restart
pkill -f scheduler.py
python src/scheduler.py --schedule daily &
```

## Key Features

- ✓ Automated training at scheduled intervals
- ✓ Model validation before deployment
- ✓ Automatic backup (last 5 versions)
- ✓ Health monitoring and metrics tracking
- ✓ Auto-reload API after training
- ✓ Logging for debugging
- ✓ Configurable via environment variables

Xem chi tiết đầy đủ trong `PIPELINE.md`
