# AI Product Recommendation Service

Hệ thống gợi ý sản phẩm sử dụng Machine Learning với thuật toán Item-Based Collaborative Filtering và Weighted Scoring.

## Tính năng chính

- **Personalized Recommendations**: Gợi ý cá nhân hóa dựa trên lịch sử mua hàng
- **Similar Products**: Tìm sản phẩm tương tự
- **Popular Products**: Sản phẩm phổ biến nhất, dùng làm fallback cho user mới
- **Weighted Scoring**: Sản phẩm được mua nhiều lần sẽ có trọng số cao hơn
- **Automatic Fallback**: Tự động chuyển sang popular products nếu không có đủ dữ liệu để tạo gợi ý cá nhân hóa
- **FastAPI + Swagger**: REST API với interactive documentation
- **CORS Support**: Sẵn sàng tích hợp với Spring Boot và React

## Cấu trúc project

```
ml-recommendation/
├── models/
│   └── item_based_cf.pkl       # Model file sau khi train
│
├── notebooks/
│   ├── data_analysis.ipynb     # Phân tích dữ liệu và lựa chọn thuật toán
│   └── model_training.ipynb    # Train model với weighted scoring
│
├── src/
│   ├── recommender.py          # Service class
│   ├── api.py                  # FastAPI endpoints
│   └── train.py                # Training script
│
├── requirements.txt
├── API_DOCUMENTATION.md         # Chi tiết API đầy đủ
└── README.md
```

## Hướng dẫn khởi động

### Bước 1: Cài đặt dependencies

```bash
cd ml-recommendation
pip install -r requirements.txt
```

Yêu cầu:
- Python 3.9 trở lên
- MySQL database để train model
- Tối thiểu 2GB RAM

### Bước 2: Train model

Cách 1 - Sử dụng Jupyter Notebook (khuyên dùng):
```bash
jupyter notebook notebooks/model_training.ipynb
# Chạy tất cả cells
```

Cách 2 - Sử dụng Python script:
```bash
python src/train.py
```

Kết quả sẽ tạo ra file `models/item_based_cf.pkl` khoảng 0.04 MB.

### Bước 3: Khởi động API server

```bash
cd src
python api.py
```

Output sẽ hiển thị:
```
INFO: Uvicorn running on http://0.0.0.0:8000
Model loaded successfully!
  Trained at: 2025-11-05T02:09:51
  Products: 33
  Users: 102
```

### Bước 4: Test API

```bash
# Chạy test script
python test_api_examples.py

# Hoặc mở Swagger UI
# http://localhost:8000/docs
```

## API Endpoints chính

| Endpoint | Mục đích |
|----------|----------|
| `/api/recommendations/similar/{id}` | Gợi ý sản phẩm tương tự (Customers also bought) |
| `/api/recommendations/user` | Gợi ý cá nhân hóa (Recommended for you) |
| `/api/recommendations/popular` | Sản phẩm phổ biến (Best Sellers, dùng cho user mới) |

Xem chi tiết đầy đủ trong file `API_DOCUMENTATION.md`.

## Cải tiến thuật toán

Phiên bản cũ sử dụng simple average (không chính xác):
```python
score = mean(similarity_A, similarity_B)
```

Phiên bản mới sử dụng weighted scoring:
```python
score = purchase_count_A × similarity_A + purchase_count_B × similarity_B
```

Kết quả: Độ chính xác của recommendations tăng gấp 6 lần.

## Automated Training Pipeline

Hệ thống hỗ trợ automated training pipeline để model tự động học và cải thiện:

- **Automated Training**: Tự động train lại model theo lịch định kỳ (daily, weekly, custom)
- **Model Validation**: Kiểm tra chất lượng model trước khi deploy
- **Backup Management**: Tự động backup model cũ (giữ 5 backups gần nhất)
- **Health Monitoring**: Theo dõi health và performance metrics
- **Auto Reload**: Tự động reload API sau khi train xong

Xem chi tiết: `PIPELINE.md`

Quick start:
```bash
# Check model health
python src/monitor.py --action health

# Manual training
python src/train.py

# Start automated scheduler (daily at 2 AM)
python src/scheduler.py --schedule daily
```

## Tài liệu tham khảo

- `PIPELINE.md` - Hướng dẫn automated training pipeline
- `API_DOCUMENTATION.md` - Hướng dẫn chi tiết về API, integration với Spring Boot
- `notebooks/model_training.ipynb` - Training notebook với các cải tiến
- `notebooks/data_analysis.ipynb` - Phân tích dữ liệu và lựa chọn thuật toán
