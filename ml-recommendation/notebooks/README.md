# Data Analysis Notebook

## Mục đích

Notebook này phân tích dữ liệu e-commerce để:
1. Đánh giá chất lượng dữ liệu
2. Phân tích hành vi người dùng và patterns của sản phẩm
3. Tính toán các metrics quan trọng (sparsity, co-occurrence)
4. Đề xuất thuật toán Machine Learning phù hợp dựa trên đặc điểm dữ liệu

## Hướng dẫn chạy

### Bước 1: Cài đặt dependencies

```bash
# Chuyển đến thư mục ml-recommendation
cd D:\work-space\HTTM\ml-recommendation

# Cài đặt packages
pip install -r requirements.txt
```

### Bước 2: Khởi động Jupyter Notebook

```bash
jupyter notebook notebooks/data_analysis.ipynb
```

Trình duyệt sẽ tự động mở notebook.

### Bước 3: Chạy notebook

Cách 1 - Chạy toàn bộ:
- Menu → Cell → Run All

Cách 2 - Chạy từng cell:
- Click vào cell
- Nhấn `Shift + Enter` để chạy và chuyển sang cell tiếp theo
- Hoặc nhấn `Ctrl + Enter` để chạy và ở lại cell hiện tại

## Kết quả mong đợi

Notebook sẽ tạo ra các phân tích sau:

### Data Quality Report
- Số lượng users, products, orders
- Số interactions (purchases, views)
- Kiểm tra missing values

### User Behavior Analysis
- Phân phối số orders per user
- Phân phối số products per user
- Top active users

### Product Analysis
- Sản phẩm phổ biến nhất
- Co-occurrence patterns (sản phẩm được mua cùng nhau)
- View statistics

### Matrix Analysis
- User-Item interaction matrix
- Sparsity metric (rất quan trọng)
- Density visualization

### Algorithm Recommendations
- Thuật toán được khuyến nghị
- Lý do lựa chọn dựa trên đặc điểm dữ liệu
- Gợi ý implementation

## Metrics quan trọng

### Sparsity
```
Sparsity = 1 - (số interactions thực tế / tổng số cells)
```

Ngưỡng tham khảo:
- Dưới 95%: Item-Based CF hoặc User-Based CF
- 95-99%: Item-Based CF với dimensionality reduction
- Trên 99%: Matrix Factorization (SVD, ALS)

### Co-occurrence Count
Số lượng product pairs được mua cùng nhau:
- Trên 100 strong pairs: Tốt cho Item-Based CF
- Dưới 50 pairs: Nên dùng content-based hoặc hybrid

### Avg Interactions per User
- Từ 5 interactions trở lên: User-Based CF khả thi
- Dưới 5: Nên tập trung vào Item-Based CF

## Thuật toán được đề xuất

Dựa trên kết quả phân tích, notebook sẽ đề xuất:

### Item-Based Collaborative Filtering
- Phù hợp với hầu hết hệ thống e-commerce
- Ổn định hơn User-Based
- Implement pattern "Customers who bought X also bought Y"
- Similarity: Cosine hoặc Jaccard

### Matrix Factorization (nếu sparsity cao)
- SVD (Singular Value Decomposition)
- ALS (Alternating Least Squares)

### Hybrid Approach (nếu có view data)
- Kết hợp purchase và view signals
- Weighted scoring

### Content-Based (fallback)
- Category similarity
- Brand affinity

## Output

Notebook tạo ra:
- Visualizations (charts, histograms)
- Statistical summaries
- Algorithm recommendation kèm giải thích
- Gợi ý implementation chi tiết

## Lưu ý

1. Database connection: Đảm bảo MySQL đang chạy
2. Docker: Container phải ở trạng thái running
3. Port: MySQL port 3306 phải available
4. Data: Đảm bảo có đủ dữ liệu trong các bảng orders, order_items, user_product_views

## Troubleshooting

### Lỗi kết nối database
```python
# Kiểm tra connection trong cell đầu tiên
# Nếu lỗi, check Docker:
docker ps
docker start [container_id]
```

### Lỗi thiếu packages
```bash
pip install --upgrade -r requirements.txt
```

### Notebook không mở
```bash
# Thử port khác
jupyter notebook --port 8889
```

## Tài liệu tham khảo

- Collaborative Filtering: https://en.wikipedia.org/wiki/Collaborative_filtering
- Item-Based CF paper: https://dl.acm.org/doi/10.1145/371920.372071
- Matrix Factorization: https://datajobs.com/data-science-repo/Recommender-Systems-[Netflix].pdf

## Bước tiếp theo

Sau khi chạy notebook này:
1. Xác nhận thuật toán phù hợp
2. Implement model training script
3. Đánh giá model performance
4. Deploy API service
