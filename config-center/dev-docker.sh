#!/bin/bash
# 启动完整的 Docker 开发环境
# 依赖: docker, docker-compose
# 使用前请确保 .env 文件已配置（参考 .env.example）

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

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

# 等待服务启动（通过容器健康检查，不暴露密码）
echo "Waiting for MySQL to be healthy..."
MAX_RETRIES=30
RETRY_COUNT=0
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if [ "$(docker inspect --format='{{.State.Health.Status}}' config-center-mysql-dev 2>/dev/null)" = "healthy" ]; then
        echo "  ✅ MySQL 已就绪"
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo "  ...等待 MySQL (${RETRY_COUNT}/${MAX_RETRIES})"
    sleep 2
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo "  ⚠️  MySQL 启动超时，请检查: docker logs config-center-mysql-dev"
fi

echo "Waiting for Redis to be healthy..."
RETRY_COUNT=0
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if [ "$(docker inspect --format='{{.State.Health.Status}}' config-center-redis-dev 2>/dev/null)" = "healthy" ]; then
        echo "  ✅ Redis 已就绪"
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo "  ...等待 Redis (${RETRY_COUNT}/${MAX_RETRIES})"
    sleep 2
done

echo "Development environment is ready!"
echo ""
echo "下一步:"
echo "  1. 启动前端: cd ../config-web && npm install && npm run dev"
echo "  2. 启动后端 (新终端): cd config-center && mvn spring-boot:run -Dspring-boot.run.profiles=dev"
echo ""
echo "访问地址:"
echo "  前端: http://localhost:8000"
echo "  后端 API: http://localhost:8080"
echo "  Adminer (MySQL管理): http://localhost:18082"
echo "  Redis Commander: http://localhost:18083"

# 显示容器状态
echo ""
echo "Container status:"
docker-compose -f docker-compose.dev.yml ps
