# ================================
# Dockerfile for ML Recommendation Service
# ================================

FROM python:3.11-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first (cache layer)
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy source code
COPY src/ ./src/
COPY models/ ./models/
COPY notebooks/ ./notebooks/

# Create logs directory
RUN mkdir -p logs

# Create non-root user
RUN useradd -m -u 1000 mluser && \
    chown -R mluser:mluser /app
USER mluser

# Expose port
EXPOSE 8000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8000/health || exit 1

# Default command: Start API server
CMD ["python", "src/api.py"]

# Alternative commands (can override in docker-compose):
# - Training: CMD ["python", "src/train.py"]
# - Scheduler: CMD ["python", "src/scheduler.py", "--schedule", "daily"]
# - Monitor: CMD ["python", "src/monitor.py", "--action", "health"]
