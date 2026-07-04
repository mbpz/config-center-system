# 部署指南

> Config Center System — 个人/企业私有化部署指南

---

## 目录

- [1. 快速开始](#1-快速开始)
- [2. Docker 部署](#2-docker-部署)
- [3. Docker Compose 部署](#3-docker-compose-部署)
- [4. Kubernetes 部署](#4-kubernetes-部署)
- [5. 环境变量参考](#5-环境变量参考)
- [6. 初始化配置](#6-初始化配置)
- [7. 验证部署](#7-验证部署)
- [8. 常见问题](#8-常见问题)

---

## 1. 快速开始

最快 30 秒启动（使用内嵌 H2 数据库，仅用于测试）:

```bash
docker run -d -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:h2:mem:config_center;DB_CLOSE_DELAY=-1 \
  -e SPRING_DATASOURCE_USERNAME=sa \
  -e SPRING_DATASOURCE_PASSWORD= \
  -e CC_ADMIN_PASSWORD=admin123 \
  dougzeng/config-center:v0.3.0
```

> ⚠️ 生产环境请使用 MySQL，H2 仅用于开发测试。

---

## 2. Docker 部署

### 2.1 单机部署

```bash
docker run -d --name config-center \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://your-mysql-host:3306/config_center?useSSL=false&serverTimezone=UTC \
  -e SPRING_DATASOURCE_USERNAME=config_user \
  -e SPRING_DATASOURCE_PASSWORD=your_db_password \
  -e SPRING_REDIS_HOST=your-redis-host \
  -e SPRING_REDIS_PASSWORD=your_redis_password \
  -e CC_ADMIN_PASSWORD=your_strong_admin_password \
  -e CC_USER_PASSWORD=your_strong_user_password \
  -e CC_MASTER_KEY=your_16_plus_chars_encryption_key \
  -e CC_CORS_ALLOWED_ORIGINS=https://your-domain.com \
  dougzeng/config-center:v0.3.0
```

### 2.2 使用 .env 文件

创建 `.env` 文件:

```env
# 数据库配置 (必填)
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/config_center?useSSL=false&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=config_user
SPRING_DATASOURCE_PASSWORD=change_me_db_password

# Redis 配置
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=change_me_redis_password
SPRING_REDIS_DATABASE=0

# 安全配置 (必填)
CC_ADMIN_PASSWORD=change_me_admin_password
CC_USER_PASSWORD=change_me_user_password
CC_MASTER_KEY=change_me_16_chars_encryption_key

# 网络配置
SERVER_PORT=8080
CC_CORS_ALLOWED_ORIGINS=*

# 加密配置
CC_ENCRYPT_PATTERNS=*.password,*.secret,*.token,*.apikey,*.private_key
```

启动:

```bash
docker run -d --name config-center \
  -p 8080:8080 \
  --env-file .env \
  dougzeng/config-center:v0.3.0
```

### 2.3 查看日志

```bash
# 查看启动日志
docker logs -f config-center

# 首次启动会打印 Bootstrap Admin Token (如果未设置 CC_ADMIN_PASSWORD)
docker logs config-center | grep "Bootstrap Admin Token"
```

---

## 3. Docker Compose 部署

### 3.1 完整栈 (推荐)

创建 `docker-compose.yml`:

```yaml
version: '3.8'

services:
  # ========== Config Center 应用 ==========
  config-center:
    image: dougzeng/config-center:v0.3.0
    container_name: config-center
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/config_center?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: ${DB_USER:-config_user}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
      SPRING_REDIS_PASSWORD: ${REDIS_PASSWORD}
      CC_ADMIN_PASSWORD: ${ADMIN_PASSWORD}
      CC_USER_PASSWORD: ${USER_PASSWORD:-user123}
      CC_MASTER_KEY: ${MASTER_KEY}
      CC_CORS_ALLOWED_ORIGINS: ${CORS_ORIGINS:-*}
      SERVER_PORT: 8080
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/v1/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
    networks:
      - config-network

  # ========== MySQL 数据库 ==========
  mysql:
    image: mysql:8.0
    container_name: config-center-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
      MYSQL_DATABASE: config_center
      MYSQL_USER: ${DB_USER:-config_user}
      MYSQL_PASSWORD: ${DB_PASSWORD}
    command: >
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --default-authentication-plugin=mysql_native_password
    volumes:
      - mysql_data:/var/lib/mysql
      - ./config-center/src/main/resources/init/mysql:/docker-entrypoint-initdb.d:ro
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${DB_ROOT_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
    ports:
      - "3306:3306"
    networks:
      - config-network

  # ========== Redis 缓存 ==========
  redis:
    image: redis:7.2-alpine
    container_name: config-center-redis
    restart: unless-stopped
    command: >
      redis-server
      --requirepass ${REDIS_PASSWORD}
      --appendonly yes
      --bind 0.0.0.0
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "-a", "${REDIS_PASSWORD}", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    ports:
      - "6379:6379"
    networks:
      - config-network

volumes:
  mysql_data:
    name: config-center-mysql-data
  redis_data:
    name: config-center-redis-data

networks:
  config-network:
    driver: bridge
```

创建 `.env`:

```env
# MySQL
DB_ROOT_PASSWORD=root_password_change_me
DB_USER=config_user
DB_PASSWORD=db_password_change_me

# Redis
REDIS_PASSWORD=redis_password_change_me

# Config Center Security (必填!)
ADMIN_PASSWORD=admin_password_change_me
USER_PASSWORD=user_password_change_me
MASTER_KEY=16_chars_min_encryption_key

# CORS
CORS_ORIGINS=*
```

启动:

```bash
docker-compose up -d

# 查看日志
docker-compose logs -f config-center

# 查看 Bootstrap Token (首次启动)
docker-compose logs config-center | grep -A 5 "Bootstrap"
```

### 3.2 仅 Config Center (已有 MySQL/Redis)

```yaml
version: '3.8'

services:
  config-center:
    image: dougzeng/config-center:v0.3.0
    ports:
      - "8080:8080"
    env_file: .env
    restart: unless-stopped
```

---

## 4. Kubernetes 部署

### 4.1 使用 Helm Chart

```bash
# 添加仓库 (本地)
helm install config-center ./config-center/deploy/helm/config-center \
  --set image.tag=v0.3.0 \
  --set config.datasource.host=mysql-service \
  --set config.datasource.password=your_db_password \
  --set config.redis.host=redis-service \
  --set config.redis.password=your_redis_password \
  --set config.security.adminPassword=your_admin_password \
  --set config.encryption.masterKey=your_master_key
```

### 4.2 使用原生清单

```bash
# 应用所有 K8s 资源
kubectl apply -f config-center/deploy/kubernetes/

# 查看 Pod 状态
kubectl get pods -l app=config-center

# 查看日志
kubectl logs -l app=config-center -f
```

### 4.3 K8s Secret 配置

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: config-center-secrets
  namespace: default
type: Opaque
stringData:
  SPRING_DATASOURCE_PASSWORD: "your_db_password"
  SPRING_REDIS_PASSWORD: "your_redis_password"
  CC_ADMIN_PASSWORD: "your_admin_password"
  CC_USER_PASSWORD: "your_user_password"
  CC_MASTER_KEY: "your_16_chars_encryption_key"
```

---

## 5. 环境变量参考

### 5.1 数据库配置

| 变量 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| `SPRING_DATASOURCE_URL` | ✅ | - | MySQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | ✅ | - | 数据库用户名 |
| `SPRING_DATASOURCE_PASSWORD` | ✅ | - | 数据库密码 |
| `SPRING_REDIS_HOST` | 否 | localhost | Redis 主机地址 |
| `SPRING_REDIS_PORT` | 否 | 6379 | Redis 端口 |
| `SPRING_REDIS_PASSWORD` | 否 | - | Redis 密码 |
| `SPRING_REDIS_DATABASE` | 否 | 0 | Redis DB 索引 |

### 5.2 安全配置

| 变量 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| `CC_ADMIN_PASSWORD` | ✅ | admin | Admin 用户密码 |
| `CC_USER_PASSWORD` | 否 | user | Viewer 用户密码 |
| `CC_MASTER_KEY` | 推荐 | - | AES-256-GCM 加密密钥 (16+ 字符) |
| `CC_CORS_ALLOWED_ORIGINS` | 否 | * | 允许的 CORS 来源 (逗号分隔) |

### 5.3 加密配置

| 变量 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| `CC_ENCRYPT_PATTERNS` | 否 | `*.password,*.secret,*.token,*.apikey,*.private_key` | 自动加密的 key 模式 |

> 匹配这些模式的 key 会自动加密存储，无需手动设置 `encrypted=true`。

### 5.4 应用配置

| 变量 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| `SERVER_PORT` | 否 | 8080 | 应用监听端口 |
| `SPRING_PROFILES_ACTIVE` | 否 | - | Spring Profile (prod/dev) |

---

## 6. 初始化配置

### 6.1 首次启动

1. **设置强密码**: 必须修改默认 `CC_ADMIN_PASSWORD`
2. **设置加密密钥**: 设置 `CC_MASTER_KEY` (16+ 字符随机字符串)
3. **数据库初始化**: 首次启动自动执行 `init/*.sql` 脚本

### 6.2 Bootstrap Token

如果未设置 `CC_ADMIN_PASSWORD`（或设为 `admin`），首次启动会生成随机 token:

```
╔══════════════════════════════════════════════════════════════╗
║  Config Center - Bootstrap Admin Token                      ║
║                                                              ║
║  Username: admin                                             ║
║  Password: AbCdEfGhIjKlMnOpQrStUvWx   ║
║                                                              ║
║  ⚠️  SAVE THIS TOKEN - it will not be shown again           ║
╚══════════════════════════════════════════════════════════════╝
```

### 6.3 默认账号

| 用户名 | 默认密码 | 角色 | 权限 |
|--------|----------|------|------|
| admin | admin / 自定义 | ADMIN | 读写所有配置 |
| user | user / 自定义 | USER | 只读访问 |

---

## 7. 验证部署

### 7.1 健康检查

```bash
# 系统健康
curl http://localhost:8080/api/v1/health

# 响应示例:
# {"status":"UP","redis_available":true,"cache_strategy":"三级缓存(本地+Redis+数据库)","timestamp":1720000000000}
```

### 7.2 认证测试

```bash
# 获取当前用户信息
curl -u admin:your_password http://localhost:8080/api/v1/auth/me

# 创建配置
curl -u admin:your_password -X POST http://localhost:8080/api/v1/configs \
  -H "Content-Type: application/json" \
  -d '{"configKey":"app.name","configValue":"My App","description":"测试","environment":"dev","version":"1.0","status":"ACTIVE"}'

# 获取配置
curl -u admin:your_password "http://localhost:8080/api/v1/configs/app.name?environment=dev"

# 列出配置
curl -u admin:your_password "http://localhost:8080/api/v1/configs?environment=dev"
```

### 7.3 Prometheus 指标

```bash
curl http://localhost:8080/actuator/prometheus
```

### 7.4 SSE 实时推送

```bash
# 监听配置变更
curl -N -u admin:your_password "http://localhost:8080/api/v1/configs/stream?environment=dev"
```

---

## 8. 常见问题

### Q1: 启动失败 "Failed to obtain JDBC Connection"

**原因**: MySQL 连接失败
**解决**:
- 检查 `SPRING_DATASOURCE_URL` 是否正确
- 确认 MySQL 已启动并可访问
- 检查防火墙/网络策略

### Q2: 配置加密失败 "EncryptionException"

**原因**: 未设置 `CC_MASTER_KEY`
**解决**:
```bash
# 生成随机密钥
openssl rand -base64 32
# 或
head -c 32 /dev/urandom | base64
```
设置环境变量 `CC_MASTER_KEY` 为生成的值。

### Q3: Redis 不可用

**原因**: Redis 服务未启动或配置错误
**解决**: 系统会自动降级到 二级缓存（本地+数据库），不影响核心功能。

### Q4: 如何修改已有配置的密码?

```bash
# 登录后通过 API 修改
curl -u admin:old_password -X PUT http://localhost:8080/api/v1/auth/password \
  -H "Content-Type: application/json" \
  -d '{"oldPassword":"old","newPassword":"new_strong_password"}'
```

### Q5: 如何备份配置?

```bash
# 导出 JSON
curl -u admin:password "http://localhost:8080/api/v1/configs/export?env=prod&format=json" > backup.json

# 导出 YAML
curl -u admin:password "http://localhost:8080/api/v1/configs/export?env=prod&format=yaml" > backup.yaml
```

### Q6: 如何恢复配置?

```bash
curl -u admin:password -X POST http://localhost:8080/api/v1/configs/import \
  -F "file=@backup.json" \
  -F "environment=prod" \
  -F "overwrite=true"
```

### Q7: 生产环境最佳实践

1. ✅ 设置强 `CC_ADMIN_PASSWORD` (16+ 字符)
2. ✅ 设置强 `CC_MASTER_KEY` (32+ 字符)
3. ✅ MySQL 和 Redis 启用密码认证
4. ✅ 使用 HTTPS (反向代理)
5. ✅ 限制 CORS 来源 (`CC_CORS_ALLOWED_ORIGINS`)
6. ✅ 定期备份数据库
7. ✅ 启用审计日志 (默认已启用)
8. ✅ 使用 K8s Secret 管理敏感配置

---

## 相关链接

- **GitHub**: https://github.com/mbpz/config-center-system
- **Docker Hub**: https://hub.docker.com/r/dougzeng/config-center
- **API 文档**: 见 [README.md](../README.md#api-接口)
- **PRD**: 见 [PRD-trust-first.md](./PRD-trust-first.md)
