#!/bin/bash

# 启动MySQL和Redis容器
echo "Starting MySQL and Redis containers..."
docker-compose up -d

# 等待MySQL和Redis启动
echo "Waiting for MySQL and Redis to start..."
sleep 10

# 检查MySQL是否就绪
echo "Checking MySQL status..."
until docker exec config-center-mysql mysqladmin ping -h localhost -u root -proot123 --silent; do
    echo "Waiting for MySQL to be ready..."
    sleep 2
done

# 检查Redis是否就绪
echo "Checking Redis status..."
until docker exec config-center-redis redis-cli -a redis123 ping; do
    echo "Waiting for Redis to be ready..."
    sleep 2
done

echo "MySQL and Redis are ready!"

# 启动Spring Boot应用
echo "Starting Spring Boot application..."
mvn spring-boot:run -Dspring-boot.run.profiles=dev 