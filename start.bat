@echo off
echo ========================================
echo 微服务教学系统启动脚本
echo ========================================
echo.

echo [1/5] 启动 Nacos 注册中心...
echo 请确保 Nacos 已安装在 127.0.0.1:8848
echo.

echo [2/5] 启动 Common 模块...
cd /d %~dp0common
call mvn clean install -DskipTests
echo.

echo [3/5] 启动 User Service (端口 8081)...
cd /d %~dp0user-service
start "UserService" cmd /k "mvn spring-boot:run"
timeout /t 5
echo.

echo [4/5] 启动 Course Service (端口 8082)...
cd /d %~dp0course-service
start "CourseService" cmd /k "mvn spring-boot:run"
timeout /t 5
echo.

echo [5/5] 启动 Homework Service (端口 8083)...
cd /d %~dp0homework-service
start "HomeworkService" cmd /k "mvn spring-boot:run"
timeout /t 5
echo.

echo 启动 Gateway API网关 (端口 8080)...
cd /d %~dp0gateway
start "Gateway" cmd /k "mvn spring-boot:run"
echo.

echo ========================================
echo 所有微服务已启动！
echo 前端地址: http://localhost:3000
echo API网关: http://localhost:8080
echo ========================================
pause
