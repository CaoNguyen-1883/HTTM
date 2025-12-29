# Docker Deployment Guide

Hướng dẫn deploy hệ thống E-Commerce bằng Docker.

---

## Yêu cầu hệ thống

Cần cài đặt trước:

- Docker phiên bản 20.10 trở lên
- Docker Compose phiên bản 2.0 trở lên
- Make (tùy chọn, để dùng shortcuts)

Kiểm tra version:
```bash
docker --version
docker-compose --version
make --version
```

Cấu hình tối thiểu:
- RAM: 4GB (khuyến nghị 8GB)
- Ổ cứng: 10GB trống (khuyến nghị 20GB)
- CPU: 2 cores (khuyến nghị 4 cores)

---

## Khởi động nhanh

### Cách 1: Dùng Makefile (khuyến nghị)

```bash
# Khởi tạo project (copy .env, build images, start services)
make init

# Xem logs
make logs

# Kiểm tra health
make health
```

### Cách 2: Dùng Docker Compose trực tiếp

```bash
# Bước 1: Copy file environment
cp .env.example .env

# Bước 2: Sửa file .env với config của bạn
nano .env

# Bước 3: Build images
docker-compose build

# Bước 4: Start services
docker-compose up -d

# Bước 5: Kiểm tra trạng thái
docker-compose ps
```

### Truy cập ứng dụng

Sau khi start xong:

| Service | URL | Mô tả |
|---------|-----|-------|
| Frontend | http://localhost | Giao diện React |
| Backend API | http://localhost:8080 | Spring Boot API |
| ML API | http://localhost:8000 | Python FastAPI |
| Swagger | http://localhost:8080/swagger-ui.html | API docs |
| MySQL | localhost:3306 | Database |
| Redis | localhost:6379 | Cache |

### Tài khoản test mặc định

Nếu đặt TEST_USERS=true trong file .env:

| Vai trò | Email | Password |
|---------|-------|----------|
| Admin | admin@ecommerce.com | Admin@123 |
| Staff | staff@ecommerce.com | Staff@123 |
| Seller | seller@ecommerce.com | Seller@123 |
| Customer | customer@ecommerce.com | Customer@123 |

---

## Kiến trúc hệ thống

```
Docker Network: ecommerce-network

┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  Frontend   │  │  Backend    │  │ ML Service  │
│   (Nginx)   │  │(Spring Boot)│  │  (FastAPI)  │
│  Port: 80   │  │ Port: 8080  │  │ Port: 8000  │
└──────┬──────┘  └──────┬──────┘  └──────┬──────┘
       │                │                │
       └────────────────┼────────────────┘
                        │
              ┌─────────┴─────────┐
              │                   │
     ┌────────▼────────┐  ┌───────▼──────┐
     │     MySQL       │  │    Redis     │
     │   Port: 3306    │  │  Port: 6379  │
     └─────────────────┘  └──────────────┘
```

### Danh sách containers

| Container | Base Image | Mục đích |
|-----------|------------|----------|
| ecommerce-frontend | nginx:1.25-alpine | Serve React app, proxy API |
| ecommerce-backend | eclipse-temurin:21-jre-alpine | Spring Boot API |
| ecommerce-ml | python:3.11-slim | ML recommendation engine |
| ecommerce-mysql | mysql:8.0 | Database chính |
| ecommerce-redis | redis:7.2-alpine | Cache layer |
| ecommerce-ml-scheduler | python:3.11-slim | Auto train ML model (optional) |

### Volumes cho data persistence

| Volume | Lưu gì | Đường dẫn |
|--------|--------|-----------|
| mysql_data | Database files | /var/lib/mysql |
| redis_data | Redis persistence | /data |
| ml_models | ML models đã train | /app/models |
| ml_logs | Training logs | /app/logs |

---

## Cấu hình môi trường

### Các biến môi trường cần thiết

Sửa file .env:

```bash
# Database
DB_NAME=ecommerce_db
DB_USERNAME=ecommerce
DB_PASSWORD=your-secure-password-here

# JWT Secret (PHẢI ĐỔI KHI PRODUCTION!)
JWT_SECRET=your-secret-key-change-this-in-production-min-256-bits

# Database Seeding
SEED_ENABLED=true          # Tự động seed database
FORCE_RESEED=false         # CẢNH BÁO: Xóa data cũ
TEST_USERS=true            # Tạo test accounts (tắt khi production)

# Spring Profile
SPRING_PROFILE=prod        # dev, test, hoặc prod

# Redis password (optional)
REDIS_PASSWORD=

# OAuth2 Google (optional)
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
```

## Deploy lên Production
### Bước 1: Chuẩn bị môi trường

```bash
# Copy và sửa file environment
cp .env.example .env
nano .env

# Quan trọng: Phải đổi những giá trị này!
# - JWT_SECRET
# - DB_PASSWORD
# - Đặt TEST_USERS=false
# - Đặt SPRING_PROFILE=prod
```

### Bước 2: Build images

```bash
docker-compose build --no-cache
```

### Bước 3: Start services

```bash
# Start tất cả services
docker-compose up -d

# HOẶC start cả ML scheduler
docker-compose --profile scheduler up -d
```

### Bước 4: Kiểm tra deployment

```bash
# Kiểm tra container status
docker-compose ps

# Xem logs
docker-compose logs -f

# Health check
make health
# hoặc
curl http://localhost:8080/actuator/health
curl http://localhost:8000/health
```

### Bước 5: Setup ban đầu

```bash
# Đợi backend khởi tạo database (khoảng 60 giây)
docker-compose logs -f backend

# Tìm dòng: "Started EcommerceApplication"

# Test login với tài khoản test (nếu TEST_USERS=true)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@ecommerce.com","password":"Admin@123"}'
```

### Bước 6: Train ML model

```bash
# Train model lần đầu (quan trọng!)
docker-compose exec ml-service python src/train.py

# Kiểm tra model info
curl http://localhost:8000/model/info

# (Optional) Bật automated retraining
docker-compose --profile scheduler up -d ml-scheduler
```

---

## Development Mode

Dành cho development với hot-reload:

### Start môi trường dev

```bash
# Dùng Makefile
make dev

# hoặc dùng Docker Compose
docker-compose -f docker-compose.dev.yml up
```

### Tính năng ở dev mode

- Frontend: Vite dev server với HMR (Hot Module Replacement)
- Backend: Source code được mount, cần rebuild thủ công
- ML Service: Uvicorn auto-reload
- Debug Port: Java debugger trên port 5005
- Volumes riêng: Dev data tách biệt với production

### Truy cập services (dev mode)

| Service | URL |
|---------|-----|
| Frontend (Vite) | http://localhost:3000 |
| Nginx Proxy | http://localhost:80 |
| Backend | http://localhost:8080 |
| ML API | http://localhost:8000 |

### Workflow khi develop

```bash
# Start dev environment
make dev

# Xem logs
make logs

# Sửa code (tự động reload cho frontend & ML)

# Với backend, cần rebuild:
docker-compose -f docker-compose.dev.yml restart backend

# Dừng dev environment
make dev-down
```

---

## Các lệnh thường dùng

### Dùng Makefile

```bash
# Hiển thị tất cả commands
make help

# Start/Stop
make up            # Start tất cả services
make down          # Stop tất cả services
make restart       # Restart tất cả services

# Logs
make logs          # Xem tất cả logs
make logs-backend  # Chỉ logs của backend
make logs-ml       # Chỉ logs của ML service

# Status
make ps            # Hiện running containers
make health        # Kiểm tra service health

# Cleanup
make clean         # Remove stopped containers
make down-volumes  # Stop và xóa tất cả data (CẢNH BÁO!)

# ML Operations
make train         # Chạy model training

# Shell Access
make shell-backend # Mở shell của backend
make shell-ml      # Mở shell của ML service
make db-shell      # Mở MySQL shell
make redis-cli     # Mở Redis CLI
```

### Dùng Docker Compose

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# Xem logs
docker-compose logs -f [service-name]

# Restart service
docker-compose restart [service-name]

# Execute command trong container
docker-compose exec [service-name] [command]

# Scale service
docker-compose up -d --scale ml-service=3

# Rebuild service cụ thể
docker-compose build [service-name]

# Pull latest images
docker-compose pull
```

---

## Xử lý sự cố

### Services không khởi động được

```bash
# Kiểm tra logs
docker-compose logs

# Kiểm tra service cụ thể
docker-compose logs backend

# Verify environment variables
docker-compose config

# Kiểm tra disk space
df -h

# Kiểm tra Docker resources
docker system df
```

### Backend không connect được MySQL

Triệu chứng: Backend báo lỗi connection

```bash
# Kiểm tra MySQL healthy chưa
docker-compose ps mysql

# Xem MySQL logs
docker-compose logs mysql

# Đợi MySQL khởi động xong (có thể mất 30-60 giây)
docker-compose logs -f backend

# Restart backend sau khi MySQL sẵn sàng
docker-compose restart backend
```

### ML Service không phản hồi

```bash
# Kiểm tra model file tồn tại chưa
docker-compose exec ml-service ls -la models/

# Train model nếu thiếu
docker-compose exec ml-service python src/train.py

# Kiểm tra ML logs
docker-compose logs ml-service

# Restart ML service
docker-compose restart ml-service
```

### Frontend không gọi được Backend

Triệu chứng: API calls bị lỗi CORS hoặc connection

```bash
# Kiểm tra nginx config
docker-compose exec frontend cat /etc/nginx/conf.d/default.conf

# Kiểm tra backend accessible
curl http://localhost:8080/actuator/health

# Restart frontend
docker-compose restart frontend
```

### Lỗi database connection

```bash
# Truy cập MySQL shell
docker-compose exec mysql mysql -u root -p

# Kiểm tra database tồn tại
SHOW DATABASES;
USE ecommerce_db;
SHOW TABLES;

# Kiểm tra user permissions
SELECT user, host FROM mysql.user;
```

### Port đã được dùng

```bash
# Tìm process đang dùng port
lsof -i :8080  # trên macOS/Linux
netstat -ano | findstr :8080  # trên Windows

# Stop service đang dùng port hoặc đổi port trong docker-compose.yml
```

### Hết dung lượng ổ cứng

```bash
# Kiểm tra Docker disk usage
docker system df

# Xóa unused images
docker image prune -a

# Xóa volumes (CẢNH BÁO: mất data!)
docker volume prune

# Cleanup toàn bộ
docker system prune -a --volumes
```

### Rebuild lại từ đầu

```bash
# Reset hoàn toàn (CẢNH BÁO: mất tất cả data!)
docker-compose down -v
docker system prune -a --volumes
cp .env.example .env
make init
```

---

## Advanced Usage

### Backup và Restore

#### Backup Database

```bash
# Backup MySQL
docker-compose exec mysql mysqldump -u ecommerce -p ecommerce_db > backup.sql

# Backup với timestamp
docker-compose exec mysql sh -c 'mysqldump -u ecommerce -p$DB_PASSWORD ecommerce_db' > backup-$(date +%Y%m%d).sql
```

#### Restore Database

```bash
# Restore từ backup
docker-compose exec -T mysql mysql -u ecommerce -p ecommerce_db < backup.sql
```

#### Backup Volumes

```bash
# Backup MySQL data volume
docker run --rm \
  -v httm_mysql_data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/mysql-backup.tar.gz /data

# Backup ML models
docker run --rm \
  -v httm_ml_models:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/ml-models-backup.tar.gz /data
```

### Performance Tuning

#### Tối ưu MySQL

Sửa docker-compose.yml:

```yaml
mysql:
  environment:
    MYSQL_INNODB_BUFFER_POOL_SIZE: 2G
  command:
    - --innodb-buffer-pool-size=2G
    - --max-connections=500
```

#### Scale Backend

```bash
# Scale backend theo chiều ngang
docker-compose up -d --scale backend=3

# Cần thêm load balancer (nginx) phía trước
```

#### Resource Limits

Sửa docker-compose.yml:

```yaml
backend:
  deploy:
    resources:
      limits:
        cpus: '2'
        memory: 2G
      reservations:
        cpus: '1'
        memory: 1G
```

### Monitoring

#### Health Checks

```bash
# Kiểm tra tất cả services
make health

# Kiểm tra từng service
curl http://localhost:8080/actuator/health | jq .
curl http://localhost:8000/health | jq .
curl http://localhost/health
```

#### Container Stats

```bash
# Real-time stats
docker stats

# Chỉ các containers cụ thể
docker stats ecommerce-backend ecommerce-mysql
```

### Setup SSL/HTTPS

1. Lấy SSL certificates (ví dụ: Let's Encrypt)
2. Mount certificates vào nginx container
3. Sửa frontend/nginx.conf:

```nginx
server {
    listen 443 ssl;
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;

    # ... rest of config
}
```

4. Update docker-compose.yml:

```yaml
frontend:
  volumes:
    - ./ssl:/etc/nginx/ssl:ro
  ports:
    - "443:443"
    - "80:80"
```

---


