# 构建与 CI 指南

> 本文档说明如何通过 GitHub Actions 自动构建，以及如何在本地手动构建出相同的结果。

---

## 目录

- [1. GitHub Actions CI](#1-github-actions-ci)
- [2. 本地手动构建](#2-本地手动构建)
- [3. CI vs 本地构建对比](#3-ci-vs-本地构建对比)

---

## 1. GitHub Actions CI

### 1.1 CI Pipeline 概览

```
┌─────────────────────────────────────────────────────────────┐
│  CI Pipeline (ci.yml)                                       │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────────┐ │
│  │ backend-test │  │ frontend-test│  │ docker-build / sbom│ │
│  │ Maven test   │  │ yarn build   │  │ Docker image      │ │
│  │ JaCoCo report│  │              │  │ CycloneDX SBOM    │ │
│  └──────────────┘  └──────────────┘  └───────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 触发方式

#### 方式一：自动触发 (PR / Push)

```yaml
# .github/workflows/ci.yml
on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
```

- **PR 创建/更新**: 自动运行 CI
- **Push 到 main**: 自动运行 CI

#### 方式二：手动触发 (workflow_dispatch)

通过 GitHub 网页:

1. 打开 https://github.com/mbpz/config-center-system/actions/workflows/ci.yml
2. 点击 **"Run workflow"** 按钮
3. 选择分支 → 点击 **"Run workflow"**

通过 GitHub CLI:

```bash
# 触发 CI (当前分支)
gh workflow run ci.yml

# 触发 CI (指定分支)
gh workflow run ci.yml --ref main

# 查看运行状态
gh run list --limit 3

# 实时查看日志
gh run watch
```

#### 方式三：Tag 触发 (自动构建镜像)

```bash
# 推送 tag 自动触发 Docker Release
git tag v0.4.0
git push origin v0.4.0
```

或手动触发:

```bash
gh workflow run docker-release.yml --ref main --field tag=v0.4.0
```

### 1.3 CI Job 说明

| Job | 说明 | 输出 |
|-----|------|------|
| `backend-test` | Maven 测试 + JaCoCo | 测试报告, coverage 报告 |
| `frontend-test` | yarn install + build | 前端构建产物 |
| `docker-build` | Docker 镜像构建 | 本地测试镜像 |
| `generate-sbom` | CycloneDX SBOM | `sbom.spdx.json` |

### 1.4 查看 CI 结果

```bash
# 列出最近的 CI 运行
gh run list --limit 5

# 查看特定运行的详情
gh run view <run-id>

# 查看失败的日志
gh run view <run-id> --log-failed

# 查看 PR 的 CI 状态
gh pr checks <pr-number>

# 实时跟踪
gh run watch
```

---

## 2. 本地手动构建

### 2.1 前置条件

| 工具 | 版本 | 用途 |
|------|------|------|
| JDK | 8+ | 编译 Java 后端 |
| Maven | 3.6+ | 依赖管理、构建 |
| Node.js | 20+ | 前端构建 |
| yarn | 1.22+ | 前端依赖 |
| Docker | 20.10+ | 镜像构建 |

### 2.2 完整构建脚本

创建 `build.sh`:

```bash
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

# 编译 + 测试 + JaCoCo 报告
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

# 安装依赖
yarn install --frozen-lockfile

# 构建生产版本
yarn build

echo "✅ 前端构建完成"
echo "   产物目录: config-web/dist/"

# ============================================================
# Step 4: Docker 镜像构建
# ============================================================
echo ""
echo "[4/5] Docker 镜像构建..."
cd "$SCRIPT_DIR/config-center"

# 构建镜像 (使用 Docker Hub 目标名)
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
```

```bash
chmod +x build.sh
```

### 2.3 分步执行

#### 后端构建

```bash
cd config-center

# 编译
mvn clean compile -B

# 运行测试
mvn test -B

# 打包 JAR
mvn package -B -DskipTests

# 查看 JaCoCo 覆盖率报告
open target/site/jacoco/index.html

# 生成 SBOM
mvn cyclonedx:makeAggregateBom -B
```

#### 前端构建

```bash
cd config-web

# 安装依赖
yarn install --frozen-lockfile

# 开发模式
yarn dev

# 生产构建
yarn build

# 预览构建产物
yarn preview
```

#### Docker 镜像构建

```bash
cd config-center

# 基础构建
docker build -t dougzeng/config-center:latest .

# 带构建参数
docker build \
  --build-arg JAR_FILE=target/config-center-*.jar \
  -t dougzeng/config-center:v0.3.0 \
  -t dougzeng/config-center:latest \
  .

# 验证镜像
docker images | grep config-center

# 测试运行
docker run -d --name test-cc -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:h2:mem:test \
  -e SPRING_DATASOURCE_USERNAME=sa \
  -e CC_ADMIN_PASSWORD=test123 \
  dougzeng/config-center:latest

# 查看日志
docker logs -f test-cc

# 停止并删除
docker rm -f test-cc
```

#### Docker 镜像推送

```bash
# 登录 Docker Hub
docker login docker.io -u dougzeng

# 标记镜像
docker tag dougzeng/config-center:latest dougzeng/config-center:v0.3.0

# 推送到 Docker Hub
docker push dougzeng/config-center:v0.3.0
docker push dougzeng/config-center:latest

# --- 或推送到 GHCR ---

# 登录 GHCR
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# 标记
docker tag dougzeng/config-center:latest ghcr.io/mbpz/config-center-system:v0.3.0

# 推送
docker push ghcr.io/mbpz/config-center-system:v0.3.0
docker push ghcr.io/mbpz/config-center-system:latest
```

### 2.4 快速验证脚本

创建 `verify.sh`:

```bash
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
TEST_COUNT=$(find config-center/target/surefire-reports -name "*.xml" 2>/dev/null | wc -l)
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
  dougzeng/config-center:latest 2>/dev/null

echo "等待启动..."
sleep 20

if curl -s http://localhost:18080/api/v1/health | grep -q "UP"; then
    echo "✅ 健康检查通过"
    curl -s http://localhost:18080/api/v1/health | python3 -m json.tool
else
    echo "❌ 健康检查失败"
fi

docker rm -f "$CONTAINER_NAME" 2>/dev/null
echo ""
echo "=== 验证完成 ==="
```

```bash
chmod +x verify.sh
```

---

## 3. CI vs 本地构建对比

| 步骤 | GitHub Actions CI | 本地手动构建 |
|------|-------------------|-------------|
| 后端测试 | `mvn clean test -B` | `mvn clean test -B` |
| 后端打包 | `mvn package -B -DskipTests` | `mvn package -B -DskipTests` |
| 前端构建 | `yarn install && yarn build` | `yarn install && yarn build` |
| Docker 构建 | `docker build -t config-center:ci-test .` | `docker build -t dougzeng/config-center:latest .` |
| SBOM 生成 | `mvn cyclonedx:makeAggregateBom -B` | `mvn cyclonedx:makeAggregateBom -B` |
| 镜像推送 | 自动推送到 ghcr.io + Docker Hub | 手动 `docker push` |
| 触发方式 | PR / Push / 手动 | 执行 `./build.sh` |

### 等价命令速查

```bash
# === 等价于 CI 的完整本地构建 ===

# 1. 后端测试 + 打包
cd config-center && mvn clean package -B && cd ..

# 2. 前端构建
cd config-web && yarn install --frozen-lockfile && yarn build && cd ..

# 3. Docker 镜像
cd config-center && docker build -t dougzeng/config-center:latest . && cd ..

# 4. SBOM
cd config-center && mvn cyclonedx:makeAggregateBom -B && cd ..

# 5. 推送 (可选)
docker push dougzeng/config-center:latest
```

---

## 常见问题

### Q: 本地构建和 CI 构建结果不一致?

**A**: 确保使用相同的 JDK 版本 (8) 和 Node 版本 (20):
```bash
java -version  # 应该是 1.8.x
node -v       # 应该是 20.x
```

### Q: Docker 构建失败 "no space left on device"?

**A**: 清理无用镜像:
```bash
docker system prune -a
```

### Q: 前端构建失败 "engine incompatible"?

**A**: 升级 Node.js 到 20+:
```bash
nvm install 20
nvm use 20
```

### Q: 如何只运行单个测试?

```bash
mvn test -Dtest=EncryptionServiceTest
mvn test -Dtest=ConfigServiceTest#createConfig_withEncrypted_encryptsValue
```

### Q: 如何跳过测试快速构建?

```bash
mvn clean package -B -DskipTests
```
