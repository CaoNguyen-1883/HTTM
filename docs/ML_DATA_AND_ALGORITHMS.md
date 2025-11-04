# Dữ liệu và Thuật toán Machine Learning

## Tổng quan

Hệ thống gợi ý sản phẩm của nền tảng E-Commerce được xây dựng dựa trên thuật toán Item-Based Collaborative Filtering kết hợp với Content-Based Filtering. Hệ thống sử dụng dữ liệu lịch sử tương tác của người dùng bao gồm dữ liệu mua hàng và dữ liệu xem sản phẩm để tạo ra các gợi ý cá nhân hóa.

---

## 1. Dữ liệu đầu vào

### 1.1. Nguồn dữ liệu

Hệ thống thu thập dữ liệu từ hai nguồn chính trong cơ sở dữ liệu MySQL:

#### 1.1.1. Dữ liệu mua hàng (Purchase Data)

**Bảng sử dụng:** `orders`, `order_items`, `product_variants`

**Câu truy vấn SQL:**
```sql
SELECT
    HEX(o.user_id) as user_id,
    HEX(pv.product_id) as product_id,
    oi.quantity,
    o.created_at as purchase_date
FROM orders o
INNER JOIN order_items oi ON o.id = oi.order_id
INNER JOIN product_variants pv ON oi.variant_id = pv.id
WHERE o.order_status IN ('CONFIRMED', 'DELIVERED', 'SHIPPED')
  AND o.is_active = 1
  AND oi.is_active = 1
ORDER BY o.created_at DESC
```

**Đặc điểm dữ liệu:**
- Chỉ lấy đơn hàng đã được xác nhận, đang giao hoặc đã giao
- Loại bỏ đơn hàng bị hủy hoặc không hợp lệ
- Bao gồm thông tin số lượng sản phẩm được mua
- Sắp xếp theo thời gian mua gần nhất

**Cấu trúc dữ liệu:**
| Trường | Kiểu dữ liệu | Mô tả |
|--------|-------------|-------|
| user_id | VARCHAR(32) | ID người dùng (Hex format) |
| product_id | VARCHAR(32) | ID sản phẩm (Hex format) |
| quantity | INTEGER | Số lượng sản phẩm đã mua |
| purchase_date | TIMESTAMP | Thời điểm mua hàng |

#### 1.1.2. Dữ liệu lượt xem (View Data)

**Bảng sử dụng:** `user_product_views`

**Câu truy vấn SQL:**
```sql
SELECT
    HEX(user_id) as user_id,
    HEX(product_id) as product_id,
    view_count,
    last_viewed_at
FROM user_product_views
```

**Đặc điểm dữ liệu:**
- Lưu trữ số lần người dùng xem một sản phẩm
- Cập nhật thời gian xem gần nhất
- Dữ liệu ẩn cho thấy sở thích của người dùng

**Cấu trúc dữ liệu:**
| Trường | Kiểu dữ liệu | Mô tả |
|--------|-------------|-------|
| user_id | VARCHAR(32) | ID người dùng (Hex format) |
| product_id | VARCHAR(32) | ID sản phẩm (Hex format) |
| view_count | INTEGER | Số lần xem sản phẩm |
| last_viewed_at | TIMESTAMP | Lần xem cuối cùng |

### 1.2. Thống kê dữ liệu

Dữ liệu được thu thập từ hệ thống production bao gồm:
- **Số lượng người dùng**: Từ 50 đến hàng nghìn người dùng
- **Số lượng sản phẩm**: Tối thiểu 10 sản phẩm
- **Số lượng tương tác**: Tổng hợp từ purchase và view records
- **Độ thưa (Sparsity)**: Thường > 99% (điển hình của hệ thống E-Commerce)

### 1.3. Xử lý dữ liệu

#### 1.3.1. Chuẩn hóa ID

Tất cả Product ID và User ID được chuyển đổi từ định dạng UUID sang Hex format để tối ưu hóa hiệu suất:

```
UUID: ba6f86d7-6ca4-4499-aee9-108f60a9a476
Hex:  BA6F86D76CA44499AEE9108F60A9A476
```

#### 1.3.2. Tổng hợp dữ liệu

**Purchase Data:**
- Nhóm theo `(user_id, product_id)`
- Tính tổng số lượng (`quantity`) cho mỗi cặp user-product
- Kết quả: Ma trận thưa với giá trị là tổng số lượng đã mua

**View Data:**
- Nhóm theo `(user_id, product_id)`
- Tính tổng số lần xem (`view_count`)
- Kết quả: Ma trận thưa với giá trị là số lần đã xem

#### 1.3.3. Trọng số hóa (Weighting)

Hệ thống áp dụng trọng số khác nhau cho hai loại tương tác:

```python
PURCHASE_WEIGHT = 1.0   # Mua hàng có trọng số cao nhất
VIEW_WEIGHT = 0.3       # Xem sản phẩm có trọng số thấp hơn
```

**Lý do:**
- Hành vi mua hàng phản ánh sở thích mạnh mẽ hơn
- Lượt xem có thể chỉ là tò mò, chưa chắc thích sản phẩm
- Tỷ lệ 1.0:0.3 đảm bảo cân bằng giữa hai tín hiệu

---

## 2. Ma trận tương tác người dùng - sản phẩm

### 2.1. Cấu trúc ma trận

Hệ thống xây dựng ma trận tương tác **User-Item Interaction Matrix** với cấu trúc:

```
Hàng (Rows):    User IDs (U1, U2, U3, ...)
Cột (Columns):  Product IDs (P1, P2, P3, ...)
Giá trị (Values): Interaction Score
```

**Ví dụ:**

|        | P1  | P2  | P3  | P4  | P5  |
|--------|-----|-----|-----|-----|-----|
| **U1** | 2.3 | 0   | 5.0 | 0   | 1.6 |
| **U2** | 0   | 3.0 | 0   | 2.0 | 0   |
| **U3** | 1.0 | 0   | 0   | 0   | 4.5 |

Trong đó:
- Giá trị > 0: Người dùng đã tương tác với sản phẩm
- Giá trị = 0: Chưa có tương tác
- Giá trị càng cao: Mức độ quan tâm càng lớn

### 2.2. Công thức tính Interaction Score

Điểm tương tác được tính theo công thức:

```
Interaction_Score(user, product) =
    (Purchase_Quantity × PURCHASE_WEIGHT) + (View_Count × VIEW_WEIGHT)
```

**Ví dụ cụ thể:**

Người dùng U1 với sản phẩm P1:
- Đã mua 2 lần (quantity = 2)
- Đã xem 1 lần (view_count = 1)

```
Score = (2 × 1.0) + (1 × 0.3) = 2.3
```

### 2.3. Đặc điểm ma trận

**Kích thước:**
- Số hàng: Tương ứng với số người dùng trong hệ thống
- Số cột: Tương ứng với số sản phẩm

**Độ thưa (Sparsity):**
```
Sparsity = 1 - (Số phần tử khác 0) / (Tổng số phần tử)
```

Thông thường:
- Sparsity > 99%: Mỗi người dùng chỉ tương tác với một phần rất nhỏ sản phẩm
- Đây là hiện tượng bình thường trong E-Commerce

**Lưu trữ:**
- Sử dụng Pandas DataFrame
- Tối ưu bộ nhớ với sparse matrix representation

---

## 3. Thuật toán Item-Based Collaborative Filtering

### 3.1. Nguyên lý hoạt động

Item-Based Collaborative Filtering dựa trên giả thuyết:

> **"Nếu người dùng thích sản phẩm A và sản phẩm A tương tự với sản phẩm B, thì người dùng có thể thích sản phẩm B"**

Thuật toán tính toán độ tương đồng giữa các sản phẩm dựa trên hành vi người dùng, không cần thông tin về nội dung sản phẩm.

### 3.2. Tính toán ma trận tương đồng (Similarity Matrix)

#### 3.2.1. Chuyển vị ma trận

Bước đầu tiên là chuyển vị User-Item Matrix thành Item-User Matrix:

```
User-Item Matrix (U × P)  →  Item-User Matrix (P × U)
```

Lý do: Tính similarity giữa các sản phẩm dựa trên vector người dùng

#### 3.2.2. Cosine Similarity

Hệ thống sử dụng **Cosine Similarity** để đo độ tương đồng giữa hai sản phẩm.

**Công thức:**

```
similarity(A, B) = cos(θ) = (A · B) / (||A|| × ||B||)
```

Trong đó:
- A, B: Vector người dùng của sản phẩm A và B
- A · B: Tích vô hướng (dot product)
- ||A||, ||B||: Độ dài vector (norm)

**Ý nghĩa:**
- Giá trị: -1 đến +1
- +1: Hai sản phẩm hoàn toàn tương đồng
- 0: Không có mối quan hệ
- -1: Hoàn toàn đối lập (hiếm gặp trong thực tế)

**Ví dụ tính toán:**

Sản phẩm P1 và P2 với vector người dùng:

```
P1: [2, 0, 1, 3, 0]  (U1 mua 2, U3 mua 1, U4 mua 3)
P2: [1, 0, 2, 2, 0]  (U1 mua 1, U3 mua 2, U4 mua 2)

A · B = (2×1) + (0×0) + (1×2) + (3×2) + (0×0) = 2 + 0 + 2 + 6 + 0 = 10
||A|| = √(4 + 0 + 1 + 9 + 0) = √14 ≈ 3.742
||B|| = √(1 + 0 + 4 + 4 + 0) = √9 = 3

similarity(P1, P2) = 10 / (3.742 × 3) ≈ 0.89
```

Kết luận: P1 và P2 rất tương đồng (0.89 gần 1)

#### 3.2.3. Ma trận Similarity

Kết quả là ma trận vuông **Product × Product**:

```
           P1    P2    P3    P4    P5
P1       1.00  0.89  0.12  0.45  0.00
P2       0.89  1.00  0.23  0.56  0.00
P3       0.12  0.23  1.00  0.78  0.34
P4       0.45  0.56  0.78  1.00  0.11
P5       0.00  0.00  0.34  0.11  1.00
```

Đặc điểm:
- Đường chéo = 1.0 (sản phẩm tương đồng hoàn toàn với chính nó)
- Ma trận đối xứng: similarity(A,B) = similarity(B,A)
- Giá trị trong khoảng [0, 1]

### 3.3. Co-occurrence Filtering

Để loại bỏ các mối tương quan yếu và nhiễu, hệ thống áp dụng **Co-occurrence Filtering**.

#### 3.3.1. Nguyên lý

Hai sản phẩm chỉ được coi là tương đồng nếu chúng xuất hiện cùng nhau trong lịch sử mua hàng của nhiều người dùng.

#### 3.3.2. Tính toán Co-occurrence Matrix

**Bước 1:** Chuyển đổi User-Item Matrix sang dạng nhị phân (binary)

```
0: Không tương tác
1: Có tương tác
```

**Bước 2:** Tính Co-occurrence bằng phép nhân ma trận

```
Co-occurrence = Binary_Matrix^T × Binary_Matrix
```

**Ví dụ:**

```
Binary Matrix:
       P1  P2  P3
U1     1   1   0
U2     0   1   1
U3     1   0   1

Co-occurrence = Binary^T × Binary:

       P1  P2  P3
P1     2   1   1    (P1 xuất hiện cùng P2 ở 1 user, cùng P3 ở 1 user)
P2     1   2   1
P3     1   1   2
```

#### 3.3.3. Áp dụng ngưỡng (Threshold)

Hệ thống đặt ngưỡng tối thiểu:

```python
MIN_CO_OCCURRENCE = 5
```

**Quy tắc lọc:**
```
IF co_occurrence(A, B) < MIN_CO_OCCURRENCE:
    similarity(A, B) = 0
```

**Lợi ích:**
- Loại bỏ tương quan ngẫu nhiên (coincidence)
- Chỉ giữ lại các mối quan hệ có ý nghĩa thống kê
- Giảm nhiễu trong recommendations

### 3.4. Weighted Scoring cho Recommendation

Khi tạo gợi ý cho người dùng, hệ thống sử dụng **Weighted Scoring Algorithm**.

#### 3.4.1. Thuật toán

**Input:**
- Danh sách sản phẩm người dùng đã mua (có thể có trùng lặp)
- Ma trận similarity đã được lọc

**Output:**
- Top K sản phẩm được gợi ý với điểm số cao nhất

**Quy trình:**

1. **Đếm tần suất mua:**
```python
product_counts = Counter(purchased_products)
# Ví dụ: {P1: 2, P3: 1, P5: 3}
```

2. **Tính điểm cho mỗi sản phẩm candidate:**
```python
FOR each purchased_product, purchase_count IN product_counts:
    FOR each candidate_product:
        IF candidate_product NOT IN purchased_products:
            score[candidate_product] += purchase_count × similarity(purchased_product, candidate_product)
```

3. **Sắp xếp và lấy Top K:**
```python
recommendations = sorted(scores, reverse=True)[:top_k]
```

#### 3.4.2. Ví dụ cụ thể

**Người dùng đã mua:**
- P1: 2 lần
- P3: 1 lần

**Similarity Matrix:**
```
     P1    P2    P3    P4    P5
P1  1.00  0.80  0.12  0.45  0.65
P3  0.12  0.23  1.00  0.78  0.34
```

**Tính điểm cho P2:**
```
score(P2) = (2 × similarity(P1, P2)) + (1 × similarity(P3, P2))
          = (2 × 0.80) + (1 × 0.23)
          = 1.60 + 0.23
          = 1.83
```

**Tính điểm cho P4:**
```
score(P4) = (2 × 0.45) + (1 × 0.78)
          = 0.90 + 0.78
          = 1.68
```

**Tính điểm cho P5:**
```
score(P5) = (2 × 0.65) + (1 × 0.34)
          = 1.30 + 0.34
          = 1.64
```

**Kết quả gợi ý:**
1. P2: 1.83 (cao nhất)
2. P4: 1.68
3. P5: 1.64

---

## 4. Fallback Strategy - Popular Products

Khi không đủ dữ liệu để tạo personalized recommendations, hệ thống sử dụng **Popular Products Strategy**.

### 4.1. Khi nào sử dụng

- Người dùng mới (cold start problem)
- Người dùng chưa có lịch sử mua hàng
- Sản phẩm đã mua không tồn tại trong model
- Không tìm thấy sản phẩm tương tự

### 4.2. Cách tính độ phổ biến (Popularity Score)

**Công thức:**
```
popularity(product) = SUM of all user interactions with product
```

**Quy trình:**
1. Tính tổng interaction score của mỗi sản phẩm theo cột trong User-Item Matrix
2. Sắp xếp giảm dần theo điểm
3. Trả về Top K sản phẩm phổ biến nhất

**Ví dụ:**

```
User-Item Matrix:
       P1   P2   P3   P4
U1     2.3  0    5.0  1.6
U2     0    3.0  0    2.0
U3     1.0  0    0    4.5

Popularity:
P1: 2.3 + 0 + 1.0 = 3.3
P2: 0 + 3.0 + 0 = 3.0
P3: 5.0 + 0 + 0 = 5.0
P4: 1.6 + 2.0 + 4.5 = 8.1

Ranking: P4 (8.1) > P3 (5.0) > P1 (3.3) > P2 (3.0)
```

---

## 5. Quy trình Training Model

### 5.1. Pipeline tổng quan

```
1. Kết nối Database
      ↓
2. Load Purchase & View Data
      ↓
3. Build Interaction Matrix
      ↓
4. Calculate Similarity Matrix (Cosine)
      ↓
5. Apply Co-occurrence Filtering
      ↓
6. Validate Model Quality
      ↓
7. Save Model (Pickle format)
      ↓
8. Backup Old Models
```

### 5.2. Chi tiết từng bước

#### 5.2.1. Kết nối Database

```python
db_config = {
    'host': 'localhost',
    'port': 3306,
    'database': 'ecommerce_db',
    'user': 'ecommerce',
    'password': '***'
}

engine = create_engine(f"mysql+pymysql://{user}:{password}@{host}:{port}/{database}")
```

#### 5.2.2. Load Data

- Thực thi SQL queries để lấy purchase data và view data
- Chuyển đổi sang Pandas DataFrame
- Log số lượng records đã load

#### 5.2.3. Build Interaction Matrix

```python
# Purchase matrix: weighted by quantity
purchase_matrix = df_purchases.groupby(['user_id', 'product_id'])['quantity'].sum()
purchase_matrix = purchase_matrix * PURCHASE_WEIGHT

# View matrix: weighted by view count
view_matrix = df_views.groupby(['user_id', 'product_id'])['view_count'].sum()
view_matrix = view_matrix * VIEW_WEIGHT

# Combine
interaction_scores = purchase_matrix.add(view_matrix, fill_value=0)
user_item_matrix = interaction_scores.unstack(fill_value=0)
```

#### 5.2.4. Calculate Similarity

```python
from sklearn.metrics.pairwise import cosine_similarity

item_user_matrix = user_item_matrix.T  # Transpose
similarity_array = cosine_similarity(item_user_matrix)

# Convert to DataFrame
similarity_matrix = pd.DataFrame(
    similarity_array,
    index=product_ids,
    columns=product_ids
)
```

#### 5.2.5. Apply Filtering

```python
# Calculate co-occurrence
binary_matrix = (user_item_matrix > 0).astype(int)
co_occurrence = binary_matrix.T @ binary_matrix

# Apply threshold
co_occurrence_mask = co_occurrence >= MIN_CO_OCCURRENCE
filtered_similarity = similarity_matrix.where(co_occurrence_mask, 0)

# Keep diagonal as 1.0
np.fill_diagonal(filtered_similarity.values, 1.0)
```

#### 5.2.6. Validation

Kiểm tra chất lượng model:
```python
# Minimum requirements
assert n_products >= 10, "Too few products"
assert n_users >= 50, "Too few users"

# Warning if sparsity too high
if sparsity > 0.99:
    logger.warning(f"High sparsity: {sparsity:.2%}")
```

#### 5.2.7. Save Model

```python
model_data = {
    'similarity_matrix': filtered_similarity,
    'product_ids': product_ids,
    'user_item_matrix': user_item_matrix,
    'trained_at': datetime.now().isoformat(),
    'n_users': len(user_item_matrix),
    'n_products': len(product_ids),
    'n_interactions': int(n_interactions),
    'sparsity': float(sparsity),
    'min_co_occurrence': MIN_CO_OCCURRENCE,
    'use_hybrid': True,
    'purchase_weight': PURCHASE_WEIGHT,
    'view_weight': VIEW_WEIGHT
}

with open('item_based_cf.pkl', 'wb') as f:
    pickle.dump(model_data, f)
```

### 5.3. Model Backup Strategy

Trước khi train model mới, hệ thống tự động backup model cũ:

```
models/
  ├── item_based_cf.pkl              (Current model)
  └── backups/
      ├── item_based_cf_20250105_143022.pkl
      ├── item_based_cf_20250104_120015.pkl
      ├── item_based_cf_20250103_091234.pkl
      ├── item_based_cf_20250102_153045.pkl
      └── item_based_cf_20250101_100230.pkl
```

**Quy tắc:**
- Giữ lại tối đa 5 bản backup gần nhất
- Tự động xóa các backup cũ hơn
- Đảm bảo có thể rollback nếu model mới có vấn đề

---

## 6. Đánh giá Model

### 6.1. Metrics được sử dụng

Hệ thống tracking các metrics sau:

| Metric | Mô tả | Giá trị mong đợi |
|--------|-------|------------------|
| n_users | Số lượng người dùng | ≥ 50 |
| n_products | Số lượng sản phẩm | ≥ 10 |
| n_interactions | Tổng số tương tác | Càng nhiều càng tốt |
| sparsity | Độ thưa của ma trận | 95-99% (bình thường) |
| training_time | Thời gian training | < 60 giây |
| model_size | Kích thước file model | Vài MB đến vài chục MB |

### 6.2. Validation Rules

**Hard Requirements:**
- Tối thiểu 10 sản phẩm
- Tối thiểu 50 người dùng

**Warnings:**
- Sparsity > 99%: Cảnh báo dữ liệu quá thưa
- Training time > 60s: Có thể cần tối ưu

### 6.3. Logging và Monitoring

Mỗi lần training, hệ thống ghi log:

```
2025-01-05 14:30:22 | INFO     | Loading model from models/item_based_cf.pkl
2025-01-05 14:30:22 | INFO     | Model loaded successfully
2025-01-05 14:30:22 | INFO     | Products: 1,245
2025-01-05 14:30:22 | INFO     | Users: 3,567
2025-01-05 14:30:22 | INFO     | Sparsity: 98.45%
```

---

## 7. Ưu điểm và Hạn chế

### 7.1. Ưu điểm

**1. Không phụ thuộc vào metadata sản phẩm:**
- Không cần mô tả, danh mục, thuộc tính
- Chỉ cần dữ liệu tương tác

**2. Khả năng khám phá (Serendipity):**
- Có thể gợi ý sản phẩm bất ngờ nhưng phù hợp
- Không bị giới hạn trong cùng danh mục

**3. Cập nhật linh hoạt:**
- Model có thể retrain định kỳ
- Phản ánh xu hướng mới nhất

**4. Hiệu quả với dữ liệu thưa:**
- Cosine similarity hoạt động tốt với sparse matrix
- Co-occurrence filtering loại bỏ nhiễu

**5. Scalability:**
- Có thể scale với hàng triệu sản phẩm
- Similarity matrix được tính trước (offline)

### 7.2. Hạn chế

**1. Cold Start Problem:**
- Sản phẩm mới không có trong model
- Người dùng mới không có lịch sử
- Giải pháp: Fallback to popular products

**2. Popularity Bias:**
- Sản phẩm phổ biến được gợi ý nhiều hơn
- Sản phẩm niche ít được recommend

**3. Computational Cost:**
- Tính similarity matrix có độ phức tạp O(n²)
- Với n sản phẩm lớn cần tối ưu

**4. Sparsity:**
- Dữ liệu quá thưa có thể giảm chất lượng
- Cần đủ số lượng tương tác

### 7.3. Cải tiến trong tương lai

**Ngắn hạn:**
- Thêm content-based features (category, brand, price range)
- Implement matrix factorization (SVD, ALS)
- A/B testing để đo lường hiệu quả

**Dài hạn:**
- Deep Learning models (Neural Collaborative Filtering)
- Real-time recommendations
- Context-aware recommendations (time, location, device)

---

## 8. Kết luận

Hệ thống gợi ý sản phẩm sử dụng thuật toán Item-Based Collaborative Filtering kết hợp với Co-occurrence Filtering và Weighted Scoring đã được thiết kế để:

1. **Tận dụng tối đa dữ liệu tương tác** từ purchase và view history
2. **Cân bằng giữa personalization và popularity** thông qua fallback strategy
3. **Đảm bảo chất lượng** thông qua validation và filtering
4. **Duy trì hiệu suất** với ma trận tính toán trước và caching
5. **Hỗ trợ continuous improvement** thông qua automated retraining

Với thiết kế này, hệ thống có khả năng phục vụ hàng triệu người dùng với độ chính xác cao và thời gian phản hồi nhanh.

---

**Tài liệu tham khảo:**

1. Sarwar, B., et al. (2001). "Item-based collaborative filtering recommendation algorithms." WWW '01.
2. Leskovec, J., Rajaraman, A., & Ullman, J. D. (2014). "Mining of Massive Datasets." Cambridge University Press.
3. Ricci, F., Rokach, L., & Shapira, B. (2015). "Recommender Systems Handbook." Springer.
4. Scikit-learn Documentation: Cosine Similarity
5. Pandas Documentation: DataFrame Operations
