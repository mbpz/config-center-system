# Config Center System

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-8%2B-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/spring--boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/react-18-61dafb.svg)](https://react.dev/)
[![Tests](https://img.shields.io/badge/tests-48%20passed-brightgreen.svg)](./config-center/src/test)
[![Coverage](https://img.shields.io/badge/coverage-41.2%25-yellow.svg)](./config-center/target/site/jacoco)

一个轻量级、安全、可审计的分布式配置中心，专为个人/企业私有化部署设计。

> **当前版本**: v0.2.0 (开发中) — Phase 1 (信任层) + Phase 2 (差异化) 已完成 ✅
> 目标是成为**个人/企业私有化部署的标杆配置中心**。

---

## ✨ 当前功能

### 🔒 安全 (Phase 1)
- **RBAC 认证**: Spring Security + 内存用户 (ADMIN/USER)
- **配置加密**: AES-256-GCM 静态加密，支持按 key 模式自动加密
- **KMS 插件**: 可插拔密钥管理 (当前: Local, 预留: Aliyun/Tencent/Vault)
- **审计追踪**: 所有配置变更的 who/when/what 完整记录

### 📦 配置管理
- **集中管理**: CRUD + 搜索 + 环境隔离 (dev/test/prod)
- **批量操作**: JSON/YAML 导入导出
- **配置分组**: 按分组管理配置
- **多租户**: tenant_id 字段预留 (当前 single-tenant)

### ⚡ 实时推送
- **SSE 推送**: Server-Sent Events 实时配置变更推送
- **灰度发布**: 按百分比分流 + 按标签匹配

### 🚀 缓存与降级
- **三级缓存**: Caffeine → Redis → MySQL
- **优雅降级**: Redis 不可用时自动降级
- **Prometheus 指标**: config_read, cache_hit/miss, redis_available

### 🖥️ 前端
- **配置管理页**: CRUD + 加密标记 + 历史弹窗
- **缓存监控页**: 系统状态 + Redis 详情 + 操作日志
- **登录页**: Bootstrap token 认证
- **i18n**: 中文/英文双语切换
- **ErrorBoundary**: 渲染错误捕获 + 重试

### 📊 运维
- **Docker Compose**: 一键启动完整环境
- **Kubernetes**: Helm Chart + HPA + Ingress + ServiceMonitor
- **CI/CD**: GitHub Actions + 分支保护 + 自动发布

---

## 🛠️ 技术栈

### 后端
- **Spring Boot 2.7.18** — 应用框架
- **Spring Security** — 认证授权 (RBAC)
- **MyBatis 2.3.1** — 数据访问层
- **MySQL 8.0** — 关系型数据库
- **Redis 7.0** — 缓存数据库
- **Caffeine 2.9.3** — 本地缓存
- **Micrometer + Prometheus** — 指标导出
- **Jackson (JSON + YAML)** — 序列化/反序列化
- **Lombok** — 代码简化

### 前端
- **React 18** — 用户界面框架
- **UmiJS 4** — 企业级前端应用框架
- **Ant Design 5** — UI 组件库
- **TypeScript** — 类型安全的 JavaScript
- **自定义 i18n** — 国际化 (zh-CN / en-US)

### DevOps
- **Docker + Docker Compose** — 容器化部署
- **Kubernetes + Helm** — 编排管理
- **GitHub Actions** — CI/CD
- **JaCoCo** — 代码覆盖率

---

## 🚀 快速开始

### 前置条件

- JDK 8+
- Maven 3.6+
- Node.js 16+
- Docker & Docker Compose

### 1. 克隆项目

```bash
git clone https://github.com/mbpz/config-center-system.git
cd config-center-system
```

### 2. 启动后端（Docker 方式）

```bash
cd config-center

# 复制环境变量模板并修改
cp .env.example .env
# 编辑 .env 填入真实值（开发环境可用默认值）

# 启动 MySQL + Redis + 管理工具
./dev-docker.sh

# 新终端：启动 Spring Boot
./dev.sh
```

### 3. 启动前端

```bash
cd config-web
npm install
npm run dev
```

### 4. 访问应用

| 服务 | 地址 |
|------|------|
| 前端界面 | http://localhost:8000 |
| 后端 API | http://localhost:8080 |
| Adminer (MySQL) | http://localhost:18082 |
| Redis Commander | http://localhost:18083 |

---

## 📊 数据库设计

### 配置项表 (config_item)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| config_key | VARCHAR(100) | 配置键 |
| config_value | TEXT | 配置值 (可能为 AES-256-GCM 密文) |
| description | VARCHAR(500) | 配置描述 |
| environment | VARCHAR(50) | 环境标识 (dev/test/prod) |
| version | VARCHAR(50) | 版本号 |
| status | VARCHAR(20) | 状态 (ACTIVE/INACTIVE) |
| encrypted | BOOLEAN | 是否加密存储 |
| tenant_id | VARCHAR(50) | 租户标识 (默认 'default') |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

> 唯一键: `(config_key, environment, tenant_id)`

### 配置变更日志表 (config_change_log)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| config_key | VARCHAR(100) | 配置键 |
| environment | VARCHAR(50) | 环境标识 |
| old_value | TEXT | 变更前值 (加密配置显示 `***ENCRYPTED***`) |
| new_value | TEXT | 变更后值 |
| change_type | VARCHAR(20) | 变更类型 (CREATE/UPDATE/DELETE) |
| operator | VARCHAR(50) | 操作人 (从 SecurityContext 获取) |
| change_time | DATETIME | 变更时间 |

### 配置分组表 (config_group)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| group_id | VARCHAR(50) | 分组唯一标识 |
| group_name | VARCHAR(100) | 分组名称 |
| description | VARCHAR(500) | 描述 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 灰度发布策略表 (config_gray_release)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| config_key | VARCHAR(100) | 配置键 |
| environment | VARCHAR(50) | 环境标识 |
| strategy_type | VARCHAR(20) | 策略类型 (PERCENTAGE/TAG) |
| strategy_detail | TEXT | JSON 策略详情 |
| gray_value | TEXT | 灰度分流后的值 |
| enabled | BOOLEAN | 是否启用 |
| operator | VARCHAR(50) | 操作人 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

---

## 🔌 API 接口

### 认证

所有 API 使用 HTTP Basic Auth (用户名/密码)。

| 角色 | 权限 |
|------|------|
| `admin` | 读写配置、导入导出、灰度发布、用户管理 |
| `user` | 只读配置、查看审计、订阅 SSE |

### 配置管理

| 方法 | 路径 | 角色 | 说明 |
|------|------|------|------|
| GET | `/api/v1/configs?environment={env}` | ADMIN/USER | 获取配置列表 |
| GET | `/api/v1/configs/{key}?environment={env}` | ADMIN/USER | 获取单个配置 (自动解密) |
| POST | `/api/v1/configs` | ADMIN | 创建配置 |
| PUT | `/api/v1/configs/{key}?environment={env}` | ADMIN | 更新配置 |
| DELETE | `/api/v1/configs/{key}?environment={env}` | ADMIN | 删除配置 |
| GET | `/api/v1/configs/export?env=&format=json\|yaml` | ADMIN | 批量导出 |
| POST | `/api/v1/configs/import` (multipart) | ADMIN | 批量导入 |
| GET | `/api/v1/configs/stream?environment={env}` | ADMIN/USER | SSE 实时推送 |

### 审计

| 方法 | 路径 | 角色 | 说明 |
|------|------|------|------|
| GET | `/api/v1/audit?key=&environment=` | ADMIN/USER | 查询变更历史 |

### 配置分组

| 方法 | 路径 | 角色 | 说明 |
|------|------|------|------|
| GET | `/api/v1/groups` | ADMIN/USER | 分组列表 |
| GET | `/api/v1/groups/{groupId}` | ADMIN/USER | 分组详情 |
| POST | `/api/v1/groups` | ADMIN | 创建分组 |
| PUT | `/api/v1/groups/{groupId}` | ADMIN | 更新分组 |
| DELETE | `/api/v1/groups/{groupId}` | ADMIN | 删除分组 |

### 灰度发布

| 方法 | 路径 | 角色 | 说明 |
|------|------|------|------|
| GET | `/api/v1/gray-releases` | ADMIN/USER | 灰度策略列表 |
| POST | `/api/v1/gray-releases` | ADMIN | 创建策略 |
| PUT | `/api/v1/gray-releases/{id}` | ADMIN | 更新策略 |
| DELETE | `/api/v1/gray-releases/{id}` | ADMIN | 删除策略 |
| POST | `/api/v1/gray-releases/{id}/toggle` | ADMIN | 启用/禁用 |

### 健康检查 & 监控

| 方法 | 路径 | 角色 | 说明 |
|------|------|------|------|
| GET | `/api/v1/health` | 公开 | 系统健康状态 |
| GET | `/api/v1/health/redis` | 公开 | Redis 健康状态 |
| GET | `/api/v1/health/cache/stats` | 公开 | 缓存统计 |
| GET | `/api/v1/health/cache/list` | 公开 | 缓存列表 |
| POST | `/api/v1/health/cache/refresh/{env}` | 公开 | 刷新指定环境缓存 |
| POST | `/api/v1/health/cache/refresh` | 公开 | 刷新所有缓存 |
| GET | `/actuator/prometheus` | 公开 | Prometheus 指标 |
| GET | `/actuator/health` | 公开 | Actuator 健康检查 |

### 认证 API

| 方法 | 路径 | 角色 | 说明 |
|------|------|------|------|
| GET | `/api/v1/auth/me` | 公开 | 当前用户信息 |
| POST | `/api/v1/auth/logout` | 公开 | 登出 |

---

## 🐳 Docker 部署

### 开发环境

```bash
cd config-center
docker-compose -f docker-compose.dev.yml up -d
```

### 生产环境

```bash
cd config-center

# 1. 配置环境变量
cp .env.example .env
vim .env  # 填入真实值

# 2. 启动
docker-compose up -d

# 3. 查看日志
docker-compose logs -f config-center
```

---

## 🔧 配置说明

所有配置通过环境变量传入，详见 `.env.example`。

### 关键环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `SPRING_DATASOURCE_URL` | MySQL 连接 URL | `jdbc:mysql://localhost:3306/config_center?useSSL=true&serverTimezone=UTC` |
| `SPRING_DATASOURCE_USERNAME` | MySQL 用户名 | `config_user` |
| `SPRING_DATASOURCE_PASSWORD` | MySQL 密码 | (必填) |
| `SPRING_REDIS_HOST` | Redis 主机 | `localhost` |
| `SPRING_REDIS_PORT` | Redis 端口 | `6379` |
| `SPRING_REDIS_PASSWORD` | Redis 密码 | (必填) |
| `SERVER_PORT` | 应用端口 | `8080` |

---

## 📁 项目结构

```
config-center-system/
├── config-center/                  # 后端服务 (Spring Boot)
│   ├── src/main/java/              # Java 源码
│   │   └── com/crgmhrc/configcenter/
│   │       ├── config/             # 配置类 (Security, KMS, SSE, Metrics)
│   │       ├── controller/         # REST 控制器
│   │       ├── service/            # 业务逻辑
│   │       ├── entity/             # 数据实体
│   │       ├── mapper/             # MyBatis 映射
│   │       └── security/           # 安全工具 (TenantContext, SecurityUtils)
│   ├── src/test/                   # 测试 (48 tests)
│   ├── src/main/resources/         # 配置文件
│   ├── init/                       # 数据库初始化脚本
│   ├── deploy/                     # 部署
│   │   ├── kubernetes/             # K8s 原生清单
│   │   └── helm/config-center/     # Helm Chart
│   ├── docker-compose.yml          # 生产编排
│   ├── docker-compose.dev.yml      # 开发编排
│   ├── .env.example                # 环境变量模板
│   └── pom.xml                     # Maven 配置
├── config-web/                     # 前端界面 (React + UmiJS)
│   ├── src/
│   │   ├── pages/                  # 页面 (Config, Cache, Login)
│   │   ├── components/             # 公共组件 (ErrorBoundary, LocaleSwitcher)
│   │   ├── context/                # 上下文 (LocaleContext)
│   │   ├── locales/                # 国际化 (zh-CN, en-US)
│   │   ├── services/               # API 服务
│   │   └── app.ts                  # 运行时配置
│   └── package.json
├── docs/
│   ├── PRD-trust-first.md          # 产品需求文档
│   ├── design.md                   # 架构设计文档
│   ├── sdk/README.md               # SDK 设计文档
│   └── operator/README.md          # K8s Operator 设计文档
├── .github/                        # GitHub 配置
│   ├── workflows/                  # CI/CD (ci.yml, release.yml)
│   ├── ISSUE_TEMPLATE/             # Issue 模板
│   └── PULL_REQUEST_TEMPLATE.md    # PR 模板
├── ROADMAP.md                      # 路线图
├── CHANGELOG.md                    # 更新日志
├── CONTRIBUTING.md                 # 贡献指南
├── CODE_OF_CONDUCT.md              # 行为准则
├── SECURITY.md                     # 安全政策
├── LICENSE                         # Apache 2.0 许可证
└── NOTICE                          # 第三方软件声明
```

---

## 🗺️ 路线图

详见 [ROADMAP.md](ROADMAP.md)。

### ✅ Phase 1: 信任层 (v0.1.0) — 已完成
- [x] LICENSE + NOTICE + SPDX + SBOM
- [x] 凭证清理 + 环境变量化
- [x] README 诚实化
- [x] Spring Security 认证 (RBAC)
- [x] 审计日志 (/api/v1/audit)
- [x] 配置值加密 (AES-256-GCM)
- [x] 测试覆盖 (48 tests, 41.2%)
- [x] CI/CD Pipeline (GitHub Actions)
- [x] K8s 部署 + Helm Chart + Prometheus
- [x] 前端整治 + i18n + ErrorBoundary
- [x] 社区脚手架 (CONTRIBUTING/COC/SECURITY)

### ✅ Phase 2: 差异化 (v0.5.0) — 已完成
- [x] 字段级加密 + 可插拔 KMS 插件
- [x] SSE 实时推送
- [x] 批量导入导出 (JSON/YAML)
- [x] 多租户支持 (tenant_id)
- [x] SDK 设计文档 (Java/Go/Python/CLI)
- [x] K8s Operator 设计文档
- [x] 配置分组与标签
- [x] 灰度发布 (百分比/标签分流)

### 🔜 Phase 3: 生态扩展 (v1.0.0) — 计划中
- Java / Go / Python SDK 实现
- Aliyun / Tencent / Vault KMS 插件
- K8s Operator (Go + Kubebuilder)
- 性能测试 + 基准报告

---

## 🤝 贡献

欢迎贡献！请先阅读 [CONTRIBUTING.md](CONTRIBUTING.md)。

开发环境搭建请参考 [DEVELOPMENT.md](config-center/DEVELOPMENT.md)。

安全漏洞请私下报告，详见 [SECURITY.md](SECURITY.md)。

---

## 📄 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可证。

Copyright 2026 mbpz

---

## 📞 联系方式

- **项目地址**: https://github.com/mbpz/config-center-system
- **问题反馈**: [GitHub Issues](https://github.com/mbpz/config-center-system/issues)
