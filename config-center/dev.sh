#!/bin/bash
# 启动开发环境（本地 MySQL + Redis + Spring Boot）
# 依赖: docker, docker-compose, maven
# 使用前请确保 .env 文件已配置（参考 .env.example）

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Load .env if present
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
fi

# 启动MySQL和Redis容器
echo "Starting MySQL and Redis containers..."
docker-compose -f docker-compose.dev.yml up -d

# 等待MySQL和Redis启动（通过容器健康检查，不暴露密码）
echo "Waiting for MySQL to be healthy..."
until [ "$(docker inspect --format='{{.State.Health.Status}}' config-center-mysql-dev 2>/dev/null)" = "healthy" ]; do
    echo "  ...等待 MySQL..."
    sleep 2
done

echo "Waiting for Redis to be healthy..."
until [ "$(docker inspect --format='{{.State.Health.Status}}' config-center-redis-dev 2>/dev/null)" = "healthy" ]; do
    echo "  ...等待 Redis..."
    sleep 2
done

echo "MySQL and Redis are ready!"

# 启动Spring Boot应用
echo "Starting Spring Boot application with dev profile..."
mvn spring-boot:run -Dspring-boot.run.profiles=dev
