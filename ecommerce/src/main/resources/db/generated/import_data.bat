@echo off
echo ==========================================
echo  Import ML Training Data
echo ==========================================
echo.
echo This will import 5 SQL files into ecommerce_db database
echo.
echo Files to import:
echo   1. users_generated.sql (100 users)
echo   2. orders_generated.sql (5,000 orders)
echo   3. order_items_generated.sql (8,780 items)
echo   4. reviews_generated.sql (2,000 reviews)
echo   5. views_generated.sql (3,167 views)
echo.
echo ==========================================
echo.

set /p password="Enter MySQL root password: "

echo.
echo [1/5] Importing users...
mysql -u root -p%password% ecommerce_db < users_generated.sql
if %errorlevel% neq 0 (
    echo ERROR: Failed to import users!
    pause
    exit /b 1
)
echo SUCCESS: Users imported!

echo.
echo [2/5] Importing orders...
mysql -u root -p%password% ecommerce_db < orders_generated.sql
if %errorlevel% neq 0 (
    echo ERROR: Failed to import orders!
    pause
    exit /b 1
)
echo SUCCESS: Orders imported!

echo.
echo [3/5] Importing order items...
mysql -u root -p%password% ecommerce_db < order_items_generated.sql
if %errorlevel% neq 0 (
    echo ERROR: Failed to import order items!
    pause
    exit /b 1
)
echo SUCCESS: Order items imported!

echo.
echo [4/5] Importing reviews...
mysql -u root -p%password% ecommerce_db < reviews_generated.sql
if %errorlevel% neq 0 (
    echo ERROR: Failed to import reviews!
    pause
    exit /b 1
)
echo SUCCESS: Reviews imported!

echo.
echo [5/5] Importing views...
mysql -u root -p%password% ecommerce_db < views_generated.sql
if %errorlevel% neq 0 (
    echo ERROR: Failed to import views!
    pause
    exit /b 1
)
echo SUCCESS: Views imported!

echo.
echo ==========================================
echo  IMPORT COMPLETE!
echo ==========================================
echo.
echo Next steps:
echo   1. Verify data: Run SQL queries to check counts
echo   2. Train ML model: cd D:\work-space\HTTM\ml-recommendation
echo   3. Run: python src/train.py
echo.
pause
