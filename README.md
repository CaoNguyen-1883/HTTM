# E-Commerce Platform với AI Recommendation System

Hệ thống thương mại điện tử đầy đủ tính năng với công nghệ gợi ý sản phẩm dựa trên Machine Learning.

---

## Giới thiệu

Đây là một nền tảng e-commerce hoàn chỉnh được xây dựng với kiến trúc microservices, bao gồm:

- **Backend API** (Spring Boot) - RESTful API với đầy đủ tính năng quản lý sản phẩm, đơn hàng, user, review
- **Frontend** (React + TypeScript) - Giao diện người dùng hiện đại, responsive
- **ML Recommendation Engine** (Python + FastAPI) - Hệ thống gợi ý sản phẩm thông minh sử dụng Item-Based Collaborative Filtering
- **Database** (MySQL) - Lưu trữ dữ liệu chính
- **Cache** (Redis) - Tối ưu hiệu suất

### Điểm nổi bật

- **AI-Powered Recommendations**: Gợi ý sản phẩm cá nhân hóa dựa trên lịch sử mua hàng và hành vi người dùng
- **Multi-Role System**: Hỗ trợ 4 vai trò (Admin, Staff, Seller, Customer) với phân quyền chi tiết
- **Real-time Caching**: Redis caching cho performance tối ưu
- **Automated ML Training**: Tự động train lại model theo lịch định kỳ
- **RESTful API**: API documentation đầy đủ với Swagger
- **Responsive Design**: Giao diện thân thiện trên mọi thiết bị

---

## Yêu cầu hệ thống

### Với Docker (Khuyến nghị)

- Docker 20.10 trở lên
- Docker Compose 2.0 trở lên
- RAM tối thiểu: 4GB (khuyến nghị 8GB)
- Ổ cứng trống: 10GB (khuyến nghị 20GB)

### Không dùng Docker

**Backend (Spring Boot):**
- Java 21
- Maven 3.9+
- MySQL 8.0
- Redis 7.x (optional)

**ML Service (Python):**
- Python 3.9+
- pip
- MySQL 8.0

**Frontend (React):**
- Node.js 20+
- npm hoặc yarn

---

## Cài đặt và chạy

### Cách 1: Dùng Docker (Đơn giản nhất)

#### Bước 1: Clone repository

```bash
git clone https://github.com/your-username/ecommerce-platform.git
cd ecommerce-platform
```

#### Bước 2: Cấu hình môi trường

```bash
# Copy file environment mẫu
cp .env.example .env

# Sửa file .env với text editor của bạn
nano .env
```

Các giá trị cần sửa trong file .env:

```bash
# Đổi password database (khuyến nghị)
DB_PASSWORD=your-secure-password-here

# Đổi JWT secret (BẮT BUỘC cho production)
JWT_SECRET=your-very-long-random-secret-key-at-least-256-bits

# Tắt test users khi production
TEST_USERS=false
```

#### Bước 3: Khởi động toàn bộ hệ thống

```bash
# Sử dụng Makefile (đơn giản)
make init

# Hoặc dùng Docker Compose trực tiếp
docker-compose build
docker-compose up -d
```

#### Bước 4: Đợi services khởi động

```bash
# Xem logs để theo dõi quá trình khởi động
make logs

# Hoặc
docker-compose logs -f
```

Đợi cho đến khi thấy dòng:
- Backend: "Started EcommerceApplication"
- ML Service: "Application startup complete"

#### Bước 5: Train ML model lần đầu

```bash
# Train model (quan trọng!)
make train

# Hoặc
docker-compose exec ml-service python src/train.py
```

#### Bước 6: Truy cập ứng dụng

| Service | URL | Mô tả |
|---------|-----|-------|
| Frontend | http://localhost | Giao diện chính |
| Backend API | http://localhost:8080 | Spring Boot API |
| Swagger UI | http://localhost:8080/swagger-ui.html | API documentation |
| ML API | http://localhost:8000 | Python FastAPI |
| ML API Docs | http://localhost:8000/docs | ML API documentation |

#### Bước 7: Đăng nhập với tài khoản test

Nếu bạn để `TEST_USERS=true` trong file .env, có thể dùng các tài khoản sau:

| Vai trò | Email | Password |
|---------|-------|----------|
| Admin | admin@ecommerce.com | Admin@123 |
| Staff | staff@ecommerce.com | Staff@123 |
| Seller | seller@ecommerce.com | Seller@123 |
| Customer | customer@ecommerce.com | Customer@123 |

---

### Cách 2: Chạy thủ công (Không dùng Docker)

#### A. Setup Database

```bash
# Cài đặt MySQL 8.0
# Tạo database
mysql -u root -p
CREATE DATABASE ecommerce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ecommerce'@'localhost' IDENTIFIED BY 'your-password';
GRANT ALL PRIVILEGES ON ecommerce_db.* TO 'ecommerce'@'localhost';
FLUSH PRIVILEGES;
EXIT;

# Cài đặt Redis (optional)
# Ubuntu/Debian
sudo apt-get install redis-server
sudo systemctl start redis

# macOS
brew install redis
brew services start redis
```

#### B. Chạy Backend (Spring Boot)

```bash
cd ecommerce

# Sửa file application.yml hoặc tạo application-dev.yml
# Cấu hình database connection

# Build và chạy
mvn clean install
mvn spring-boot:run

# Hoặc chạy jar file
java -jar target/ecommerce-0.0.1-SNAPSHOT.jar
```

Backend sẽ chạy trên: http://localhost:8080

#### C. Chạy ML Service (Python)

```bash
cd ml-recommendation

# Tạo virtual environment
python -m venv venv
source venv/bin/activate  # Linux/Mac
# hoặc
venv\Scripts\activate  # Windows

# Cài đặt dependencies
pip install -r requirements.txt

# Cấu hình database connection (environment variables)
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=ecommerce_db
export DB_USER=ecommerce
export DB_PASSWORD=your-password

# Train model lần đầu
python src/train.py

# Chạy API server
python src/api.py
```

ML API sẽ chạy trên: http://localhost:8000

#### D. Chạy Frontend (React)

```bash
cd frontend

# Cài đặt dependencies
npm install

# Chạy development server
npm run dev
```

Frontend sẽ chạy trên: http://localhost:3000

**Lưu ý:** Khi chạy thủ công, bạn cần cấu hình CORS và API endpoints trong frontend:
- Tạo file `.env.local` trong folder frontend
- Thêm: `VITE_API_URL=http://localhost:8080/api`
- Thêm: `VITE_ML_API_URL=http://localhost:8000`

---

## Cấu trúc project

```
ecommerce-platform/
│
├── ecommerce/                  # Backend Spring Boot
│   ├── src/main/java/         # Source code Java
│   │   └── dev/CaoNguyen_1883/ecommerce/
│   │       ├── auth/          # Authentication & JWT
│   │       ├── user/          # User management
│   │       ├── product/       # Product, Category, Brand
│   │       ├── cart/          # Shopping cart
│   │       ├── order/         # Order management
│   │       ├── review/        # Product reviews
│   │       ├── recommendation/ # Recommendation integration
│   │       ├── tracking/      # User behavior tracking
│   │       ├── config/        # Configuration classes
│   │       └── common/        # Shared utilities
│   ├── src/main/resources/    # Config files
│   │   └── application.yml    # Main config
│   ├── pom.xml                # Maven dependencies
│   └── Dockerfile             # Docker configuration
│
├── ml-recommendation/          # ML Recommendation Service
│   ├── src/
│   │   ├── api.py             # FastAPI endpoints
│   │   ├── recommender.py     # ML algorithms
│   │   ├── train.py           # Model training script
│   │   ├── scheduler.py       # Auto-retraining scheduler
│   │   └── monitor.py         # Model monitoring
│   ├── models/                # Trained ML models
│   ├── notebooks/             # Jupyter notebooks
│   ├── requirements.txt       # Python dependencies
│   └── Dockerfile             # Docker configuration
│
├── frontend/                   # React Frontend
│   ├── src/
│   │   ├── components/        # React components
│   │   │   ├── layout/        # Layout components
│   │   │   ├── shared/        # Shared components
│   │   │   ├── customer/      # Customer components
│   │   │   ├── admin/         # Admin components
│   │   │   └── staff/         # Staff components
│   │   ├── pages/             # Page components
│   │   │   ├── auth/          # Login, Register
│   │   │   ├── public/        # Public pages
│   │   │   ├── customer/      # Customer pages
│   │   │   ├── seller/        # Seller pages
│   │   │   ├── admin/         # Admin pages
│   │   │   └── staff/         # Staff pages
│   │   ├── lib/               # Libraries
│   │   │   ├── api/           # API clients
│   │   │   ├── hooks/         # React hooks
│   │   │   ├── stores/        # State management
│   │   │   └── types/         # TypeScript types
│   │   └── routes/            # Route configuration
│   ├── package.json           # npm dependencies
│   ├── nginx.conf             # Nginx configuration
│   └── Dockerfile             # Docker configuration
│
├── scripts/                    # Utility scripts
│   ├── init-db.sql            # Database initialization
│   └── nginx-dev.conf         # Nginx dev config
│
├── docker-compose.yml          # Production deployment
├── docker-compose.dev.yml      # Development deployment
├── .env.example               # Environment variables template
├── Makefile                   # Docker shortcuts
├── DOCKER-README.md           # Docker documentation
└── README.md                  # This file
```

---

## Tính năng chính

### 1. Quản lý User và Authentication

- Đăng ký, đăng nhập với email/password
- Đăng nhập với Google OAuth2
- JWT-based authentication với access token và refresh token
- Phân quyền theo vai trò (RBAC):
  - **Admin**: Quản lý toàn bộ hệ thống
  - **Staff**: Duyệt sản phẩm, quản lý đơn hàng, kiểm duyệt review
  - **Seller**: Đăng bán sản phẩm, quản lý sản phẩm của mình
  - **Customer**: Mua sắm, đánh giá sản phẩm

### 2. Quản lý Sản phẩm

- CRUD sản phẩm với variants (size, color, etc.)
- Nhiều ảnh cho mỗi sản phẩm
- Quản lý danh mục phân cấp (category hierarchy)
- Quản lý thương hiệu (brands)
- Workflow phê duyệt sản phẩm (Seller tạo -> Staff/Admin duyệt)
- Quản lý tồn kho
- Tìm kiếm và lọc sản phẩm

### 3. Giỏ hàng và Thanh toán

- Thêm sản phẩm vào giỏ hàng
- Chọn variants (size, color)
- Cập nhật số lượng
- Checkout với thông tin giao hàng
- Nhiều phương thức thanh toán (COD, Bank Transfer, Credit Card, MoMo, ZaloPay)

### 4. Quản lý Đơn hàng

- Theo dõi trạng thái đơn hàng:
  - PENDING -> CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED
- Hủy đơn hàng
- Lịch sử đơn hàng
- Chi tiết đơn hàng với tracking info
- Thống kê doanh thu (cho Admin)

### 5. Hệ thống Review

- Đánh giá sản phẩm với rating 1-5 sao
- Upload ảnh trong review
- Verified purchase badge
- Vote helpful/not helpful
- Seller có thể reply review
- Staff/Admin kiểm duyệt review

### 6. AI Recommendation System (Tính năng đặc biệt)

Sử dụng Machine Learning để gợi ý sản phẩm:

- **Personalized Recommendations**: "Dành riêng cho bạn"
  - Dựa trên lịch sử mua hàng của user
  - Weighted scoring: sản phẩm mua nhiều lần có trọng số cao hơn

- **Similar Products**: "Khách hàng cũng mua"
  - Sản phẩm thường được mua cùng nhau
  - Hiển thị ở trang chi tiết sản phẩm

- **Popular Products**: "Sản phẩm phổ biến"
  - Fallback cho user mới chưa có lịch sử
  - Dựa trên số lượng mua và view

**Thuật toán:**
- Item-Based Collaborative Filtering
- Cosine Similarity
- Co-occurrence filtering
- Hybrid approach (purchase + view data)

**Automated Training:**
- Tự động train lại model theo lịch (daily/weekly)
- Model validation trước khi deploy
- Backup management
- Health monitoring

### 7. Caching và Performance

- Redis caching cho:
  - Products (1 giờ)
  - Categories và Brands (2 giờ)
  - Users (15 phút)
  - Orders (10 phút)
  - Search results (15 phút)
- Automatic cache invalidation
- Fallback to Caffeine (in-memory) nếu Redis unavailable

### 8. Admin Dashboard

- Thống kê tổng quan
- Quản lý users
- Quản lý sản phẩm
- Quản lý đơn hàng
- Kiểm duyệt reviews
- Báo cáo doanh thu

---

## Tech Stack

### Backend

- **Framework**: Spring Boot 3.4.1
- **Language**: Java 21
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA / Hibernate
- **Caching**: Redis 7.2
- **Security**: Spring Security + JWT
- **OAuth2**: Google Login
- **API Docs**: Swagger/OpenAPI 3
- **Build Tool**: Maven

### ML Service

- **Framework**: FastAPI
- **Language**: Python 3.11
- **ML Library**: scikit-learn
- **Data Processing**: pandas, numpy
- **Database**: SQLAlchemy + PyMySQL
- **Scheduler**: APScheduler
- **Server**: Uvicorn (ASGI)

### Frontend

- **Framework**: React 19.1
- **Language**: TypeScript 5.9
- **Build Tool**: Vite 7.1
- **Routing**: React Router 7.9
- **State Management**: Zustand 5.0
- **Server State**: TanStack Query 5.90
- **Styling**: TailwindCSS 3.4
- **UI Components**: Radix UI
- **Form**: React Hook Form + Zod
- **HTTP Client**: Axios

### Infrastructure

- **Containerization**: Docker
- **Orchestration**: Docker Compose
- **Web Server**: Nginx
- **Reverse Proxy**: Nginx

---

## API Documentation

### Backend API (Spring Boot)

Sau khi chạy backend, truy cập:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

Các endpoint chính:

**Authentication:**
- POST `/api/auth/login` - Đăng nhập
- POST `/api/auth/register` - Đăng ký
- POST `/api/auth/refresh-token` - Refresh token
- GET `/api/auth/me` - Thông tin user hiện tại

**Products:**
- GET `/api/products` - Danh sách sản phẩm
- GET `/api/products/{id}` - Chi tiết sản phẩm
- POST `/api/products` - Tạo sản phẩm (SELLER)
- PUT `/api/products/{id}` - Cập nhật sản phẩm
- GET `/api/products/search?keyword={keyword}` - Tìm kiếm

**Orders:**
- GET `/api/orders/my-orders` - Đơn hàng của tôi
- POST `/api/orders` - Tạo đơn hàng
- GET `/api/orders/{id}` - Chi tiết đơn hàng
- POST `/api/orders/{id}/cancel` - Hủy đơn hàng

**Reviews:**
- GET `/api/reviews/product/{productId}` - Reviews của sản phẩm
- POST `/api/reviews` - Tạo review
- PUT `/api/reviews/{id}` - Cập nhật review

**Cart:**
- GET `/api/cart` - Giỏ hàng hiện tại
- POST `/api/cart/items` - Thêm vào giỏ
- PUT `/api/cart/items/{itemId}` - Cập nhật số lượng
- DELETE `/api/cart/items/{itemId}` - Xóa khỏi giỏ

### ML API (Python FastAPI)

Sau khi chạy ML service, truy cập:
- API Docs: http://localhost:8000/docs
- Redoc: http://localhost:8000/redoc

Các endpoint chính:

- GET `/health` - Health check
- GET `/model/info` - Thông tin model
- GET `/api/recommendations/similar/{product_id}` - Sản phẩm tương tự
- GET `/api/recommendations/user/{user_id}` - Gợi ý cá nhân hóa
- GET `/api/recommendations/popular` - Sản phẩm phổ biến
- POST `/api/model/retrain` - Train lại model

---

## Development

### Chạy môi trường development với hot-reload

```bash
# Dùng Docker Compose dev
make dev

# Hoặc
docker-compose -f docker-compose.dev.yml up
```

### Chạy tests

**Backend:**
```bash
cd ecommerce
mvn test
```

**Frontend:**
```bash
cd frontend
npm test
```

### Xem logs

```bash
# Tất cả services
make logs

# Backend only
make logs-backend

# ML service only
make logs-ml

# Frontend only
make logs-frontend
```

### Train ML model thủ công

```bash
# Với Docker
make train

# Hoặc
docker-compose exec ml-service python src/train.py

# Không Docker
cd ml-recommendation
python src/train.py
```

### Kiểm tra health

```bash
make health

# Hoặc thủ công
curl http://localhost:8080/actuator/health
curl http://localhost:8000/health
curl http://localhost/health
```

---

## Troubleshooting

### Backend không kết nối được database

```bash
# Kiểm tra MySQL đã chạy chưa
docker-compose ps mysql

# Xem logs
docker-compose logs mysql

# Restart backend
docker-compose restart backend
```

### ML service không có model

```bash
# Train model
make train

# Kiểm tra model file
docker-compose exec ml-service ls -la models/
```

### Frontend không gọi được API

```bash
# Kiểm tra nginx config
docker-compose exec frontend cat /etc/nginx/conf.d/default.conf

# Kiểm tra backend accessible
curl http://localhost:8080/actuator/health
```

### Port đã được sử dụng

```bash
# Tìm process đang dùng port
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Stop service hoặc đổi port trong docker-compose.yml
```

Xem thêm chi tiết trong [DOCKER-README.md](DOCKER-README.md)

---

## Đóng góp

Contributions are welcome! Please follow these steps:

1. Fork repository
2. Tạo branch mới: `git checkout -b feature/your-feature`
3. Commit changes: `git commit -am 'Add some feature'`
4. Push to branch: `git push origin feature/your-feature`
5. Tạo Pull Request

---

## License

This project is licensed under the MIT License.

---

## Tác giả

- Cao Nguyen
- Email: your-email@example.com
- GitHub: [@your-username](https://github.com/your-username)

---

## Acknowledgments

- Spring Boot team
- React team
- FastAPI team
- scikit-learn team
- Tất cả contributors

---

## Screenshots

Coming soon...

---

## Roadmap

- [ ] Payment gateway integration (Stripe, PayPal)
- [ ] Email notifications
- [ ] WebSocket for real-time notifications
- [ ] Mobile app (React Native)
- [ ] Advanced analytics dashboard
- [ ] Multi-language support
- [ ] Product comparison feature
- [ ] Wishlist functionality

---

Chúc bạn sử dụng thành công! Nếu gặp vấn đề, vui lòng tạo issue trên GitHub.
