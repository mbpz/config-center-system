#!/bin/bash

# 确保脚本在错误时退出
set -e

echo "Starting local development environment..."

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running"
    exit 1
fi

# 停止并删除已存在的容器（如果存在）
echo "Cleaning up existing containers..."
docker-compose -f docker-compose.dev.yml down

# 启动开发环境
echo "Starting development containers..."
docker-compose -f docker-compose.dev.yml up -d

# 等待服务启动
echo "Waiting for services to start..."
sleep 10

# 检查MySQL是否就绪
echo "Checking MySQL status..."
until docker exec config-center-mysql-dev mysqladmin ping -h localhost -u root -proot123 --silent; do
    echo "Waiting for MySQL to be ready..."
    sleep 2
done

# 检查Redis是否就绪
echo "Checking Redis status..."
until docker exec config-center-redis-dev redis-cli -a redis123 ping; do
    echo "Waiting for Redis to be ready..."
    sleep 2
done

echo "Development environment is ready!"
echo "MySQL is accessible at localhost:3306"
echo "Redis is accessible at localhost:6379"
echo "Adminer (MySQL管理工具) is available at http://localhost:8081"
echo "Redis Commander is available at http://localhost:8082"

# 显示容器状态
echo "Container status:"
docker-compose -f docker-compose.dev.yml ps 