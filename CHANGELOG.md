# 更新日志

本项目所有重要变更都将记录在此。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
版本号遵循 [Semantic Versioning](https://semver.org/)。

---

## [Unreleased]

### 🔮 Phase 3 计划中
- [ ] Java / Go / Python SDK
- [ ] Aliyun / Tencent / Vault KMS 插件
- [ ] K8s Operator 实现 (Go + Kubebuilder)
- [ ] 网络界面重构
- [ ] 性能测试 + 基准报告

---

## [0.2.0] - 2026-07-04

> Phase 1 + Phase 2 合并发布

### 🔒 Security
- 添加 Spring Security 基础框架（ROLE_ADMIN / ROLE_USER 内存认证）
- 配置写操作需要 ADMIN 角色（/api/v1/configs/**）
- 健康检查端点允许匿名访问
- **KMS 插件接口** (KmsProvider) + LocalKmsProvider (AES-256-GCM)
- **按 key 模式自动加密** (默认: *.password, *.secret, *.token, *.apikey)
- EncryptionException 统一异常
- Bootstrap token 机制 (首次启动随机生成)
- AES-256-GCM 配置值静态加密
- /actuator/health, /info, /prometheus 公开供监控采集
- 审计日志记录所有配置变更（who/when/what）
- Bootstrap token 机制（首次启动随机生成 admin 密码）
- 移除 git 历史中的真实凭证（需原持有人轮换）
- .env 加入 .gitignore

### ✨ Added
- Apache 2.0 许可证 + NOTICE 文件
- SPDX 许可证头（所有 Java/TS/TSX 源文件）
- CycloneDX SBOM 生成（Maven + CI）
- GitHub Actions CI Pipeline（test + coverage + docker build + SBOM）
- GitHub Actions Release Pipeline（自动 Docker 推送 ghcr.io + GitHub Release）
- Kubernetes 部署清单（Deployment/Service/ConfigMap/Secret/HPA）
- Helm Chart（完整 values + templates + ServiceMonitor）
- Prometheus 指标（config_read Timer, cache_hit/miss, redis_available）
- 前端 i18n（中文/英文，useLocale hook）
- 前端 ErrorBoundary 组件（渲染错误捕获 + 重试）
- GitHub PR/Issue 模板
- 分支保护规则（PR + review + CI）
- **SSE 实时推送** (Server-Sent Events, /api/v1/configs/stream)
- **批量导入导出** (JSON/YAML, ConfigImportExportService)
- **配置分组 CRUD** (ConfigGroupController, /api/v1/groups)
- **灰度发布策略** (百分比/标签匹配, /api/v1/gray-releases)
- **多租户预留** (tenant_id 字段, TenantContext, TenantAwareMapper)
- **SDK 设计文档** (docs/sdk/README.md, Java/Go/Python/Shell)
- **K8s Operator 设计文档** (docs/operator/README.md, CRD + 架构)

### 📝 Documentation
- README 诚实化（删除虚假承诺，准确描述当前功能）
- ROADMAP.md（Phase 1 + Phase 2 路线图）
- PRD（docs/PRD-trust-first.md）
- CONTRIBUTING.md（贡献指南）
- CODE_OF_CONDUCT.md（行为准则）
- SECURITY.md（安全政策）
- CHANGELOG.md（本文件）

### ♻️ Changed
- 所有配置通过环境变量读取（无硬编码凭证）
- application.yml / application-dev.yml 移除真实 IP 和密码
- deploy-remote.sh 改用环境变量占位符
- Docker healthcheck 不再暴露密码到 CLI
- Spring Security authorizeRequests 规则细化

### 🗑️ Removed
- 前端 Home/, Table/ 占位页面
- 前端 services/demo/ Mock 数据
- README 中未实现的功能声明（SSO/RBAC/K8s/批量导入导出/推送）
- MyBatis stdout 日志（避免配置值泄漏）

### 🐛 Fixed
- type-aliases-package 旧包名 com.example → com.crgmhrc
- LocalCacheConfig @Primary 导致与 fallback CacheManager 冲突
- application.yml 中 ${VAR:} 空默认值循环引用

### ✅ Tests
- EncryptionServiceTest (12 tests)
- ConfigServiceTest (8 tests)
- CacheServiceFallbackTest (19 tests)
- SecurityConfigTest (9 tests)
- JaCoCo 覆盖率门槛 40%（核心包 ≥ 60%）

---

## [0.0.0] - 2026-07-04

### 📝 Note
初始版本（pre-release）。已完成的 Phase 1 改造使得项目从"功能原型"
转变为"可通过企业安全审查"的状态。

首个稳定版本目标: v1.0.0 (Phase 1 + Phase 2 完成后)

---

## 版本对比

| 特性 | v0.0.0 (原始) | v0.2.0 (当前) |
|------|:---:|:---:|
| LICENSE + 许可证 | ❌ | ✅ |
| 认证 (RBAC) | ❌ | ✅ |
| 审计日志 | ❌ | ✅ |
| 配置加密 (AES-256) | ❌ | ✅ |
| KMS 插件接口 | ❌ | ✅ |
| 测试 | ❌ | ✅ (48 tests) |
| 覆盖率 | 0% | ✅ 41.2% |
| CI/CD | ❌ | ✅ |
| K8s + Helm | ❌ | ✅ |
| Prometheus | ❌ | ✅ |
| SSE 推送 | ❌ | ✅ |
| 批量导入导出 | ❌ | ✅ |
| 配置分组 | ❌ | ✅ |
| 灰度发布 | ❌ | ✅ |
| 多租户 (预留) | ❌ | ✅ |
| i18n | ❌ | ✅ |
| ErrorBoundary | ❌ | ✅ |
| 社区文档 | ❌ | ✅ |
| SBOM | ❌ | ✅ |
