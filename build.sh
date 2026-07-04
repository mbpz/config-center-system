#!/bin/bash
set -euo pipefail

######################################################################
# Config Center System - 本地完整构建脚本
# 执行结果等价于 GitHub Actions CI
######################################################################

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "============================================"
echo "  Config Center System - 本地构建"
echo "============================================"

# ============================================================
# Step 1: 后端构建 + 测试
# ============================================================
echo ""
echo "[1/5] 后端构建 + 测试..."
cd "$SCRIPT_DIR/config-center"

mvn clean test -B

echo "✅ 后端测试通过"
echo "   测试报告: config-center/target/surefire-reports/"
echo "   覆盖率报告: config-center/target/site/jacoco/index.html"

# ============================================================
# Step 2: 后端 JAR 打包
# ============================================================
echo ""
echo "[2/5] 后端 JAR 打包..."
mvn package -B -DskipTests

JAR_FILE=$(ls target/config-center-*.jar 2>/dev/null | head -1)
echo "✅ JAR 打包完成: $JAR_FILE"

# ============================================================
# Step 3: 前端构建
# ============================================================
echo ""
echo "[3/5] 前端构建..."
cd "$SCRIPT_DIR/config-web"

yarn install --frozen-lockfile
yarn build

echo "✅ 前端构建完成"
echo "   产物目录: config-web/dist/"

# ============================================================
# Step 4: Docker 镜像构建
# ============================================================
echo ""
echo "[4/5] Docker 镜像构建..."
cd "$SCRIPT_DIR/config-center"

docker build -t dougzeng/config-center:latest .

echo "✅ Docker 镜像构建完成"
echo "   镜像: dougzeng/config-center:latest"

# ============================================================
# Step 5: SBOM 生成
# ============================================================
echo ""
echo "[5/5] SBOM 生成..."
mvn cyclonedx:makeAggregateBom -B -q

SBOM_FILE=$(ls target/*.sbom.json 2>/dev/null | head -1)
echo "✅ SBOM 生成完成: $SBOM_FILE"

# ============================================================
# 完成
# ============================================================
echo ""
echo "============================================"
echo "  构建完成!"
echo "============================================"
echo ""
echo "产出物:"
echo "  JAR:     config-center/target/config-center-*.jar"
echo "  前端:    config-web/dist/"
echo "  Docker:  dougzeng/config-center:latest"
echo "  SBOM:    $SBOM_FILE"
echo "  测试报告: config-center/target/surefire-reports/"
echo "  覆盖率:   config-center/target/site/jacoco/"
echo ""
echo "运行镜像:"
echo "  docker run -d -p 8080:8080 dougzeng/config-center:latest"
echo "============================================"
