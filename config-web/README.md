# 配置中心前端

基于 UmiJS + Ant Design 的配置管理前端界面。

> **当前状态**: v0.0.0 (pre-release) — 正在按 [Phase 1 信任层路线图](../ROADMAP.md) 改造中。

---

## 当前功能

- 📋 配置列表展示（按环境筛选、按配置键搜索）
- ➕ 新增配置
- ✏️ 编辑配置
- 🗑️ 删除配置
- 🌍 多环境支持（dev / test / prod）
- 📊 系统状态监控（健康检查、Redis 状态）
- 🗄️ 缓存管理（缓存列表、按环境刷新、全量刷新）

---

## 技术栈

- React 18
- UmiJS 4
- Ant Design 5
- TypeScript
- Less

---

## 开发环境

### 前置条件

- Node.js 16+
- npm 或 Yarn

### 安装与启动

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

访问 http://localhost:8000 查看应用。

> 后端需运行在 http://localhost:8080，详见 [config-center/README.md](../config-center/README.md)。

### 构建生产版本

```bash
npm run build
```

---

## 项目结构

```
src/
├── pages/              # 页面组件
│   ├── Config/         # 配置管理页面（核心功能）
│   └── Cache/          # 缓存监控页面
├── services/           # API 服务
│   └── config.ts       # 配置相关 API
├── components/         # 公共组件
│   └── Guide/          # 引导组件
├── models/             # 数据模型
├── utils/              # 工具函数
├── constants/          # 常量定义
├── access.ts           # 权限控制
└── app.ts              # 运行时配置
```

---

## API 接口

### 配置管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/configs` | 获取配置列表 |
| GET | `/api/v1/configs/{key}` | 获取单个配置 |
| POST | `/api/v1/configs` | 创建配置 |
| PUT | `/api/v1/configs/{key}` | 更新配置 |
| DELETE | `/api/v1/configs/{key}` | 删除配置 |

### 健康检查

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/health` | 系统健康状态 |
| GET | `/api/v1/health/redis` | Redis 健康状态 |
| GET | `/api/v1/health/cache/stats` | 缓存统计 |
| GET | `/api/v1/health/cache/list` | 缓存列表 |

### 缓存管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/health/cache/refresh/{environment}` | 刷新指定环境缓存 |
| POST | `/api/v1/health/cache/refresh` | 刷新所有环境缓存 |

---

## 缓存策略

- **三级缓存**: 本地缓存 (Caffeine) → Redis → MySQL
- **本地缓存**: 5 分钟过期，最大 1000 条
- **Redis 缓存**: 1 小时过期
- **自动降级**: Redis 不可用时自动降级到数据库

---

## 注意事项

1. 确保后端服务在 http://localhost:8080 运行
2. 确保 MySQL 和 Redis 服务正常运行
3. 不同环境的配置是独立的
4. 缓存刷新操作会重新加载所有配置数据

---

## 许可证

[Apache License 2.0](../LICENSE)
