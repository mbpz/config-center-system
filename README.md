# 配置中心系统 (Config Center System)

一个完整的分布式配置中心解决方案，包含后端服务（config-center）和前端管理界面（config-web），提供配置的集中管理、动态更新、版本管理、缓存管理等功能。

## 🏗️ 系统架构

```
config-center-system/
├── config-center/          # 后端服务 (Spring Boot)
│   ├── src/main/java/      # Java 源码
│   ├── src/main/resources/ # 配置文件
│   ├── init/              # 数据库初始化脚本
│   ├── docker-compose.yml # Docker 编排配置
│   └── pom.xml            # Maven 配置
├── config-web/            # 前端界面 (React + UmiJS)
│   ├── src/               # 前端源码
│   ├── package.json       # Node.js 依赖
│   └── tsconfig.json      # TypeScript 配置
└── README.md              # 项目文档
```

## ✨ 功能特性

### 🔧 配置管理
- **集中管理**: 配置的 CRUD 操作
- **多环境支持**: 开发、测试、生产环境隔离
- **版本控制**: 配置版本管理和历史记录
- **动态更新**: 实时配置更新和推送
- **批量操作**: 支持批量导入导出配置

### 🚀 缓存管理
- **三级缓存策略**: 本地缓存 → Redis 缓存 → 数据库
- **智能缓存**: 自动缓存失效和刷新
- **缓存监控**: 实时缓存状态监控
- **缓存操作**: 支持按环境刷新缓存
- **降级机制**: Redis 不可用时自动降级到数据库

### 🎨 用户界面
- **现代化 UI**: 基于 Ant Design 5 的现代化界面
- **响应式设计**: 支持各种屏幕尺寸
- **实时更新**: 配置变更实时反映到界面
- **操作便捷**: 直观的配置管理操作
- **状态监控**: 系统状态和缓存状态可视化

### 🔒 安全特性
- **数据验证**: 完整的输入验证和错误处理
- **事务支持**: 数据库操作事务保证
- **连接池**: 优化的数据库连接池配置
- **健康检查**: 完整的服务健康检查机制

## 🛠️ 技术栈

### 后端技术
- **Spring Boot 2.7.18** - 应用框架
- **MyBatis** - 数据访问层
- **MySQL 8.0** - 关系型数据库
- **Redis 7.0** - 缓存数据库
- **Caffeine** - 本地缓存
- **Docker** - 容器化部署
- **Maven** - 依赖管理

### 前端技术
- **React 18** - 用户界面框架
- **UmiJS 4** - 企业级前端应用框架
- **Ant Design 5** - UI 组件库
- **TypeScript** - 类型安全的 JavaScript
- **Less** - CSS 预处理器

## 🚀 快速开始

### 环境要求

- **JDK 8+**
- **Maven 3.6+**
- **Node.js 16+**
- **Docker & Docker Compose**
- **MySQL 8.0+**
- **Redis 7.0+**

### 1. 启动后端服务

```bash
# 进入后端目录
cd config-center

# 启动开发环境（包含数据库和Redis）
./dev.sh

# 或者使用 Docker Compose
docker-compose -f docker-compose.dev.yml up -d
```

### 2. 启动前端服务

```bash
# 进入前端目录
cd config-web

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 3. 访问应用

- **前端界面**: http://localhost:8000
- **后端 API**: http://localhost:8080
- **数据库管理**: http://localhost:18082 (Adminer)
- **Redis 管理**: http://localhost:18083 (Redis Commander)

## 📊 数据库设计

### 配置项表 (config_item)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| config_key | VARCHAR(255) | 配置键，唯一标识 |
| config_value | TEXT | 配置值 |
| description | VARCHAR(500) | 配置描述 |
| environment | VARCHAR(50) | 环境标识 |
| version | VARCHAR(50) | 版本号 |
| status | VARCHAR(20) | 状态（ACTIVE/INACTIVE） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

## 🔌 API 接口

### 配置管理接口

#### 获取配置
```http
GET /api/v1/configs/{key}?environment={env}
```

#### 获取配置列表
```http
GET /api/v1/configs?environment={env}
```

#### 创建配置
```http
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
```

#### 更新配置
```http
PUT /api/v1/configs/{key}
Content-Type: application/json

{
    "configValue": "Updated Value",
    "description": "Updated description"
}
```

#### 删除配置
```http
DELETE /api/v1/configs/{key}?environment={env}
```

### 健康检查接口

#### 系统健康状态
```http
GET /api/v1/health
```

#### Redis 健康状态
```http
GET /api/v1/health/redis
```

### 缓存管理接口

#### 刷新指定环境缓存
```http
POST /api/v1/health/cache/refresh/{environment}
```

#### 刷新所有环境缓存
```http
POST /api/v1/health/cache/refresh
```

## 🐳 Docker 部署

### 开发环境

```bash
# 启动所有服务（数据库、Redis、管理工具）
docker-compose -f docker-compose.dev.yml up -d

# 查看服务状态
docker-compose -f docker-compose.dev.yml ps

# 查看服务日志
docker-compose -f docker-compose.dev.yml logs -f
```

### 生产环境

```bash
# 构建镜像
docker build -t config-center:latest .

# 启动生产环境
docker-compose up -d
```

## 🔧 配置说明

### 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/config_center
    username: config_user
    password: config123
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### Redis 配置

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: redis123
    database: 0
    timeout: 2000ms
    jedis:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 0
```

### 缓存配置

```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=5m
```

## 🎯 使用指南

### 配置管理

1. **查看配置**: 在配置列表页面查看所有配置项
2. **环境切换**: 使用环境选择器切换不同环境
3. **搜索配置**: 使用搜索框按配置键搜索
4. **新增配置**: 点击"新增配置"按钮创建新配置
5. **编辑配置**: 点击表格中的"编辑"按钮修改配置
6. **删除配置**: 点击表格中的"删除"按钮删除配置

### 缓存管理

1. **查看缓存状态**: 在配置管理页面查看缓存状态
2. **刷新缓存**: 使用缓存管理功能刷新缓存
3. **监控缓存**: 在缓存管理页面监控缓存使用情况
4. **缓存操作**: 支持按环境或全部刷新缓存

## 🔍 监控和调试

### 数据库访问

```bash
# MySQL 命令行访问
docker exec -it config-center-mysql-dev mysql -u config_user -p

# 查看配置数据
mysql> use config_center;
mysql> select * from config_item;
```

### Redis 访问

```bash
# Redis 命令行访问
docker exec -it config-center-redis-dev redis-cli -a redis123

# 查看缓存数据
127.0.0.1:6379> keys config:*
127.0.0.1:6379> get config:app.name:dev
```

### 日志查看

```bash
# 查看应用日志
docker-compose logs -f config-center

# 查看数据库日志
docker-compose logs -f mysql

# 查看 Redis 日志
docker-compose logs -f redis
```

## 🚨 常见问题

### 1. 端口冲突
- **MySQL 端口冲突**: 修改 `docker-compose.dev.yml` 中的端口映射
- **Redis 端口冲突**: 修改 `docker-compose.dev.yml` 中的端口映射
- **应用端口冲突**: 修改 `application-dev.yml` 中的 `server.port`

### 2. 连接问题
- 检查 Docker 容器是否正常运行
- 检查网络连接和防火墙设置
- 验证数据库和 Redis 连接配置

### 3. 缓存问题
- 清除 Redis 缓存: `docker exec -it config-center-redis-dev redis-cli -a redis123 FLUSHALL`
- 应用重启会自动清除本地缓存
- 检查 Redis 连接状态和配置

## 📈 性能优化

### 缓存策略
- **本地缓存**: 5分钟过期，最大1000条记录
- **Redis 缓存**: 1小时过期，支持持久化
- **数据库**: 最终数据源，支持事务

### 数据库优化
- 使用连接池管理数据库连接
- 合理设置连接池参数
- 定期清理过期数据

### 监控建议
- 监控缓存命中率
- 监控数据库连接数
- 监控 Redis 内存使用
- 设置告警机制

## 🔄 开发指南

### 后端开发

1. **环境配置**: 使用 `dev` profile 进行开发
2. **数据库操作**: 使用事务保证数据一致性
3. **缓存使用**: 合理设置缓存时间和策略
4. **API 设计**: 遵循 RESTful 设计原则

### 前端开发

1. **组件开发**: 使用 TypeScript 和 Ant Design 组件
2. **状态管理**: 使用 UmiJS 的状态管理
3. **API 调用**: 使用统一的 API 服务
4. **样式开发**: 使用 Less 预处理器

## 📝 更新日志

### v1.0.0 (2024-01-01)
- ✨ 初始版本发布
- 🎯 基础配置管理功能
- 🚀 缓存管理功能
- 🎨 现代化用户界面
- 📊 完整的监控和调试功能

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

- **项目维护者**: [您的姓名]
- **邮箱**: [您的邮箱]
- **项目地址**: [项目仓库地址]

---

**注意**: 生产环境部署时请修改默认密码，建议启用数据库 SSL 连接，并根据实际需求调整配置参数。

本地调试
config-web npm run start
config-center mvn clean spring-boot:run -Dspring-boot.run.profiles=devls