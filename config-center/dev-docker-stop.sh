#!/bin/bash

echo "Stopping development environment..."

# 停止并删除容器
docker-compose -f docker-compose.dev.yml down

echo "Development environment has been stopped." 