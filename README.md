# Config Center System

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-8%2B-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/spring--boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/react-18-61dafb.svg)](https://react.dev/)

一个轻量级分布式配置中心，提供配置的集中管理、多环境支持、三级缓存和优雅降级。

> **当前状态**: v0.0.0 (pre-release) — 正在按 [Phase 1 信任层路线图](ROADMAP.md) 改造中。
> 目标是成为**个人/企业私有化部署的标杆配置中心**。

---

## ✨ 当前功能

### 配置管理
- **集中管理**: 配置的 CRUD 操作（键值对 + 描述 + 状态）
- **多环境支持**: dev / test / prod 环境隔离
- **搜索过滤**: 按配置键搜索、按环境筛选

### 缓存管理
- **三级缓存**: Caffeine 本地缓存 → Redis 缓存 → MySQL 数据库
- **优雅降级**: Redis 不可用时自动降级到本地缓存 + 数据库
- **缓存监控**: 实时查看缓存状态、命中率、Redis 连接状态
- **缓存操作**: 按环境或全量刷新缓存

### 用户界面
- **配置管理页**: 完整的 CRUD 表格（Ant Design 5）
- **缓存监控页**: 系统状态、Redis 详情、缓存列表、操作日志

---

## 🛠️ 技术栈

### 后端
- **Spring Boot 2.7.18** — 应用框架
- **MyBatis 2.3.1** — 数据访问层
- **MySQL 8.0** — 关系型数据库
- **Redis 7.0** — 缓存数据库
- **Caffeine 2.9.3** — 本地缓存
- **Lombok** — 代码简化

### 前端
- **React 18** — 用户界面框架
- **UmiJS 4** — 企业级前端应用框架
- **Ant Design 5** — UI 组件库
- **TypeScript** — 类型安全的 JavaScript

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
| config_key | VARCHAR(255) | 配置键，唯一标识（与 environment 联合唯一） |
| config_value | TEXT | 配置值 |
| description | VARCHAR(500) | 配置描述 |
| environment | VARCHAR(50) | 环境标识 (dev/test/prod) |
| version | VARCHAR(50) | 版本号 |
| status | VARCHAR(20) | 状态（ACTIVE/INACTIVE） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 配置变更日志表 (config_change_log)

> ⚠️ 当前 schema 已创建，审计写入功能正在开发中（见 Issue #6）。

| 字段 | 类型 | 说明 |
|------|------|------|
| change_id | BIGINT | 主键 |
| config_key | VARCHAR(255) | 配置键 |
| old_value | TEXT | 变更前值 |
| new_value | TEXT | 变更后值 |
| operator | VARCHAR(100) | 操作人 |
| change_time | DATETIME | 变更时间 |

---

## 🔌 API 接口

### 配置管理

```http
# 获取配置
GET /api/v1/configs/{key}?environment={env}

# 获取配置列表
GET /api/v1/configs?environment={env}

# 创建配置
POST /api/v1/configs
Content-Type: application/json
{
    "configKey": "app.name",
    "configValue": "Config Center",
    "description": "Application name",
    "environment": "dev",
    "version": "1.0",
    "status": "ACTIVE"
}

# 更新配置
PUT /api/v1/configs/{key}?environment={env}

# 删除配置
DELETE /api/v1/configs/{key}?environment={env}
```

### 健康检查

```http
# 系统健康状态
GET /api/v1/health

# Redis 健康状态
GET /api/v1/health/redis

# 缓存统计
GET /api/v1/health/cache/stats

# 缓存列表
GET /api/v1/health/cache/list

# 刷新指定环境缓存
POST /api/v1/health/cache/refresh/{environment}

# 刷新所有环境缓存
POST /api/v1/health/cache/refresh
```

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
├── config-center/              # 后端服务 (Spring Boot)
│   ├── src/main/java/          # Java 源码
│   │   └── com/crgmhrc/configcenter/
│   │       ├── config/         # 配置类 (Redis, Cache, Security)
│   │       ├── controller/     # REST 控制器
│   │       ├── service/        # 业务逻辑
│   │       ├── entity/         # 数据实体
│   │       └── mapper/         # MyBatis 映射
│   ├── src/main/resources/     # 配置文件
│   ├── init/                   # 数据库初始化脚本
│   ├── docker-compose.yml      # 生产编排
│   ├── docker-compose.dev.yml  # 开发编排
│   ├── .env.example            # 环境变量模板
│   └── pom.xml                 # Maven 配置
├── config-web/                 # 前端界面 (React + UmiJS)
│   ├── src/
│   │   ├── pages/              # 页面
│   │   │   ├── Config/         # 配置管理页
│   │   │   └── Cache/          # 缓存监控页
│   │   ├── services/           # API 服务
│   │   └── app.ts              # 运行时配置
│   └── package.json
├── docs/
│   ├── PRD-trust-first.md      # 产品需求文档
│   └── design.md               # 架构设计文档
├── ROADMAP.md                  # 路线图
├── LICENSE                     # Apache 2.0 许可证
└── NOTICE                      # 第三方软件声明
```

---

## 🗺️ 路线图

详见 [ROADMAP.md](ROADMAP.md)。

### Phase 1: 信任层 (v0.1.0) — 进行中 🚧
- [x] LICENSE + NOTICE
- [x] 凭证清理 + 环境变量化
- [x] README 诚实化
- [ ] Spring Security 认证
- [ ] 审计日志
- [ ] 配置值加密
- [ ] 测试覆盖
- [ ] CI/CD Pipeline
- [ ] K8s 部署 + Prometheus 指标
- [ ] 前端整治 + i18n
- [ ] 社区脚手架

### Phase 2: 差异化 (v0.5.0) — 计划中
- [ ] 字段级加密 + 可插拔 KMS
- [ ] SSE 实时推送
- [ ] 批量导入导出
- [ ] 多租户
- [ ] 多语言 SDK
- [ ] K8s Operator
- [ ] 配置分组与标签
- [ ] 灰度发布

---

## 🤝 贡献

欢迎贡献！请先阅读 [CONTRIBUTING.md](CONTRIBUTING.md)（编写中）。

开发环境搭建请参考 [DEVELOPMENT.md](config-center/DEVELOPMENT.md)。

---

## 📄 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可证。

Copyright 2026 mbpz

---

## 📞 联系方式

- **项目地址**: https://github.com/mbpz/config-center-system
- **问题反馈**: [GitHub Issues](https://github.com/mbpz/config-center-system/issues)
