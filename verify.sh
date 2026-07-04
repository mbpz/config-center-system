#!/bin/bash
set -euo pipefail

######################################################################
# 验证构建结果
######################################################################

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== 验证构建结果 ==="

# 1. 检查 JAR
echo -n "JAR 文件: "
if ls config-center/target/config-center-*.jar 1>/dev/null 2>&1; then
    echo "✅ 存在"
    ls -lh config-center/target/config-center-*.jar | awk '{print "  Size: "$5}'
else
    echo "❌ 不存在"
fi

# 2. 检查前端产物
echo -n "前端产物: "
if [ -d "config-web/dist" ] && [ "$(ls -A config-web/dist)" ]; then
    echo "✅ 存在"
    ls config-web/dist/ | head -5
else
    echo "❌ 不存在"
fi

# 3. 检查 Docker 镜像
echo -n "Docker 镜像: "
if docker images | grep -q "dougzeng/config-center"; then
    echo "✅ 存在"
    docker images | grep "dougzeng/config-center" | head -3
else
    echo "❌ 不存在"
fi

# 4. 检查 SBOM
echo -n "SBOM: "
if ls config-center/target/*.sbom.json 1>/dev/null 2>&1; then
    echo "✅ 存在"
    ls -lh config-center/target/*.sbom.json | awk '{print "  Size: "$5}'
else
    echo "❌ 不存在"
fi

# 5. 检查测试报告
echo -n "测试报告: "
TEST_COUNT=$(find config-center/target/surefire-reports -name "*.xml" 2>/dev/null | wc -l | tr -d ' ')
if [ "$TEST_COUNT" -gt 0 ]; then
    echo "✅ 存在 ($TEST_COUNT 个测试文件)"
else
    echo "❌ 不存在"
fi

# 6. 运行 Docker 健康检查
echo ""
echo "=== Docker 健康检查 ==="
CONTAINER_NAME="verify-cc-$$"

docker run -d --name "$CONTAINER_NAME" -p 18080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:h2:mem:test \
  -e SPRING_DATASOURCE_USERNAME=sa \
  -e CC_ADMIN_PASSWORD=test \
  dougzeng/config-center:latest 2>/dev/null || true

echo "等待启动..."
sleep 20

if curl -s http://localhost:18080/api/v1/health 2>/dev/null | grep -q "UP"; then
    echo "✅ 健康检查通过"
    curl -s http://localhost:18080/api/v1/health
else
    echo "❌ 健康检查失败 (服务可能仍在启动)"
fi

docker rm -f "$CONTAINER_NAME" 2>/dev/null || true
echo ""
echo "=== 验证完成 ==="
