#!/bin/bash

# 检查远程服务是否可访问
echo "Checking remote services..."

# 检查MySQL
if ! nc -z 192.168.1.100 3306; then
    echo "Error: Cannot connect to MySQL at 192.168.1.100:3306"
    exit 1
fi

# 检查Redis
if ! nc -z 192.168.1.100 6379; then
    echo "Error: Cannot connect to Redis at 192.168.1.100:6379"
    exit 1
fi

echo "Remote services are accessible!"

# 启动Spring Boot应用
echo "Starting Spring Boot application..."
mvn spring-boot:run -Dspring-boot.run.profiles=dev 