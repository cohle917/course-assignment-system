@echo off
echo 正在启动教学管理系统...

cd c:\Users\12426\Desktop\cupk2\1

echo [1/5] 编译 common 模块...
cd common
call mvn clean install -DskipTests -q
if %errorlevel% neq 0 (
    echo 编译失败，请检查错误信息
    pause
    exit /b 1
)

echo [2/5] 启动 User Service...
start "User Service" cmd /k "cd c:\Users\12426\Desktop\cupk2\1\user-service && mvn spring-boot:run"
timeout /t 8 /nobreak >nul

echo [3/5] 启动 Course Service...
start "Course Service" cmd /k "cd c:\Users\12426\Desktop\cupk2\1\course-service && mvn spring-boot:run"
timeout /t 8 /nobreak >nul

echo [4/5] 启动 Homework Service...
start "Homework Service" cmd /k "cd c:\Users\12426\Desktop\cupk2\1\homework-service && mvn spring-boot:run"
timeout /t 8 /nobreak >nul

echo [5/5] 启动 Gateway...
start "Gateway" cmd /k "cd c:\Users\12426\Desktop\cupk2\1\gateway && mvn spring-boot:run"

echo 服务启动中...
echo 请确保 Nacos 已运行在 http://127.0.0.1:8848
echo API 网关地址: http://localhost:8080
pause