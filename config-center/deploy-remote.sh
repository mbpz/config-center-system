#!/bin/bash

# 远程服务器配置
REMOTE_HOST="192.168.1.100"
REMOTE_USER="your-username"
REMOTE_DIR="/opt/config-center"

# 创建远程目录
ssh $REMOTE_USER@$REMOTE_HOST "mkdir -p $REMOTE_DIR"

# 复制docker-compose文件到远程服务器
scp docker-compose.yml $REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/
scp -r init $REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/

# 在远程服务器上启动服务
ssh $REMOTE_USER@$REMOTE_HOST "cd $REMOTE_DIR && docker-compose up -d"

# 等待服务启动
echo "Waiting for services to start..."
sleep 10

# 检查MySQL是否就绪
echo "Checking MySQL status..."
until ssh $REMOTE_USER@$REMOTE_HOST "docker exec config-center-mysql mysqladmin ping -h localhost -u root -proot123 --silent"; do
    echo "Waiting for MySQL to be ready..."
    sleep 2
done

# 检查Redis是否就绪
echo "Checking Redis status..."
until ssh $REMOTE_USER@$REMOTE_HOST "docker exec config-center-redis redis-cli -a redis123 ping"; do
    echo "Waiting for Redis to be ready..."
    sleep 2
done

echo "Services are ready on remote server!"
echo "MySQL is accessible at $REMOTE_HOST:3306"
echo "Redis is accessible at $REMOTE_HOST:6379" 