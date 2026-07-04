#!/bin/bash
# 远程服务器部署脚本
# 用法: ./deploy-remote.sh
# 前提: 在远程服务器上提前配置好 ~/.config-center/.env 文件
#
# 环境变量:
#   REMOTE_HOST     远程服务器地址
#   REMOTE_USER     远程 SSH 用户名
#   REMOTE_DIR      远程部署目录

set -euo pipefail

# 远程服务器配置（请修改为真实值或通过环境变量传入）
REMOTE_HOST="${REMOTE_HOST:-your-server-ip}"
REMOTE_USER="${REMOTE_USER:-your-username}"
REMOTE_DIR="${REMOTE_DIR:-/opt/config-center}"

echo "============================================"
echo "  Config Center - 远程部署"
echo "============================================"
echo "目标: ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}"
echo ""

# 1. 创建远程目录
echo "[1/5] 创建远程目录..."
ssh "${REMOTE_USER}@${REMOTE_HOST}" "mkdir -p ${REMOTE_DIR}"

# 2. 复制 docker-compose 文件
echo "[2/5] 复制文件到远程服务器..."
scp docker-compose.yml "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}/"
scp -r init "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}/"

# 3. 提示用户检查远程 .env 文件
echo "[3/5] 检查远程环境配置..."
echo "  ⚠️  请确保远程服务器 ${REMOTE_DIR}/.env 已配置好真实凭证"
ssh "${REMOTE_USER}@${REMOTE_HOST}" "ls -la ${REMOTE_DIR}/.env" || {
    echo ""
    echo "❌ 错误: 远程服务器上没有找到 .env 文件"
    echo "   请在远程服务器执行:"
    echo "     ssh ${REMOTE_USER}@${REMOTE_HOST}"
    echo "     mkdir -p ${REMOTE_DIR}"
    echo "     cp .env.example ${REMOTE_DIR}/.env"
    echo "     vim ${REMOTE_DIR}/.env  # 修改为真实值"
    exit 1
}

# 4. 启动服务
echo "[4/5] 启动服务..."
ssh "${REMOTE_USER}@${REMOTE_HOST}" "cd ${REMOTE_DIR} && docker-compose up -d --build"

# 5. 等待 MySQL 就绪（通过容器内健康检查，不暴露密码）
echo "[5/5] 等待服务就绪..."
echo "等待 MySQL..."
MAX_RETRIES=30
RETRY_COUNT=0
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if ssh "${REMOTE_USER}@${REMOTE_HOST}" "docker inspect --format='{{.State.Health.Status}}' config-center-mysql" 2>/dev/null | grep -q "healthy"; then
        echo "  ✅ MySQL 已就绪"
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo "  ...等待 MySQL (${RETRY_COUNT}/${MAX_RETRIES})"
    sleep 2
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo "  ⚠️  MySQL 启动超时，请手动检查: ssh ${REMOTE_USER}@${REMOTE_HOST} 'docker logs config-center-mysql'"
fi

echo "等待 Redis..."
RETRY_COUNT=0
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if ssh "${REMOTE_USER}@${REMOTE_HOST}" "docker inspect --format='{{.State.Health.Status}}' config-center-redis" 2>/dev/null | grep -q "healthy"; then
        echo "  ✅ Redis 已就绪"
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo "  ...等待 Redis (${RETRY_COUNT}/${MAX_RETRIES})"
    sleep 2
done

echo ""
echo "============================================"
echo "  部署完成!"
echo "  前端: http://${REMOTE_HOST}:8080"
echo "  管理: ssh ${REMOTE_USER}@${REMOTE_HOST} 'docker-compose -f ${REMOTE_DIR}/docker-compose.yml logs -f'"
echo "============================================"
