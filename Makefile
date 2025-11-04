# ================================
# Makefile for Docker Compose
# ================================
# Shortcuts cho các lệnh Docker thường dùng

.PHONY: help build up down logs clean restart ps

.DEFAULT_GOAL := help

## help: Hiển thị trợ giúp
help:
	@echo "====================================================="
	@echo "  Các lệnh Docker cho E-Commerce Platform"
	@echo "====================================================="
	@echo ""
	@echo "Các lệnh có sẵn:"
	@echo ""
	@grep -E '^## ' Makefile | sed 's/## /  /'
	@echo ""

## build: Build tất cả Docker images
build:
	@echo "Building Docker images..."
	docker-compose build

## up: Khởi động tất cả services
up:
	@echo "Starting services..."
	docker-compose up -d
	@echo "Services đã start!"
	@echo ""
	@echo "Truy cập:"
	@echo "  Frontend: http://localhost"
	@echo "  Backend API: http://localhost:8080"
	@echo "  ML API: http://localhost:8000"
	@echo "  Swagger: http://localhost:8080/swagger-ui.html"

## up-scheduler: Start tất cả services kể cả ML scheduler
up-scheduler:
	@echo "Starting services với ML scheduler..."
	docker-compose --profile scheduler up -d

## down: Dừng tất cả services
down:
	@echo "Stopping services..."
	docker-compose down
	@echo "Services đã dừng"

## down-volumes: Dừng services và xóa volumes (CẢNH BÁO: mất data!)
down-volumes:
	@echo "CẢNH BÁO: Sẽ xóa toàn bộ data!"
	@echo "Nhấn Ctrl+C để hủy, hoặc đợi 5 giây để tiếp tục..."
	@sleep 5
	docker-compose down -v
	@echo "Services và volumes đã bị xóa"

## logs: Xem logs của tất cả services
logs:
	docker-compose logs -f

## logs-backend: Xem logs của backend
logs-backend:
	docker-compose logs -f backend

## logs-ml: Xem logs của ML service
logs-ml:
	docker-compose logs -f ml-service

## logs-frontend: Xem logs của frontend
logs-frontend:
	docker-compose logs -f frontend

## restart: Restart tất cả services
restart:
	@echo "Restarting services..."
	docker-compose restart
	@echo "Services đã restart"

## restart-backend: Restart backend
restart-backend:
	docker-compose restart backend

## restart-ml: Restart ML service
restart-ml:
	docker-compose restart ml-service

## ps: Hiển thị running containers
ps:
	docker-compose ps

## clean: Xóa stopped containers và dangling images
clean:
	@echo "Cleaning up..."
	docker-compose down --remove-orphans
	docker system prune -f
	@echo "Cleanup hoàn tất"

## dev: Khởi động môi trường development
dev:
	@echo "Starting development environment..."
	docker-compose -f docker-compose.dev.yml up

## dev-build: Build và start development environment
dev-build:
	@echo "Building development environment..."
	docker-compose -f docker-compose.dev.yml up --build

## dev-down: Dừng development environment
dev-down:
	docker-compose -f docker-compose.dev.yml down

## train: Chạy ML model training
train:
	@echo "Starting model training..."
	docker-compose exec ml-service python src/train.py

## health: Kiểm tra health của tất cả services
health:
	@echo "Checking service health..."
	@echo ""
	@echo "Backend:"
	@curl -s http://localhost:8080/actuator/health | jq '.' || echo "Backend không available"
	@echo ""
	@echo "ML Service:"
	@curl -s http://localhost:8000/health | jq '.' || echo "ML service không available"
	@echo ""
	@echo "Frontend:"
	@curl -s http://localhost/health || echo "Frontend không available"

## shell-backend: Mở shell trong backend container
shell-backend:
	docker-compose exec backend sh

## shell-ml: Mở shell trong ML service container
shell-ml:
	docker-compose exec ml-service sh

## db-shell: Mở MySQL shell
db-shell:
	docker-compose exec mysql mysql -u ecommerce -p ecommerce_db

## redis-cli: Mở Redis CLI
redis-cli:
	docker-compose exec redis redis-cli

## init: Khởi tạo project (copy .env, build, start)
init:
	@echo "Initializing project..."
	@if [ ! -f .env ]; then \
		cp .env.example .env; \
		echo "Đã tạo file .env"; \
		echo "Vui lòng sửa file .env với cấu hình của bạn"; \
	fi
	@echo "Building images..."
	@docker-compose build
	@echo "Images đã build xong"
	@echo "Starting services..."
	@docker-compose up -d
	@echo ""
	@echo "====================================================="
	@echo "Project đã khởi tạo thành công!"
	@echo "====================================================="
	@echo ""
	@echo "Services:"
	@echo "  Frontend: http://localhost"
	@echo "  Backend API: http://localhost:8080"
	@echo "  ML API: http://localhost:8000"
	@echo "  Swagger: http://localhost:8080/swagger-ui.html"
	@echo ""
	@echo "Lệnh hữu ích:"
	@echo "  make logs        - Xem logs"
	@echo "  make ps          - Hiện running containers"
	@echo "  make health      - Kiểm tra service health"
	@echo "  make help        - Hiện tất cả lệnh"
	@echo ""
