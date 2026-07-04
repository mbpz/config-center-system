# 更新日志

本项目所有重要变更都将记录在此。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
版本号遵循 [Semantic Versioning](https://semver.org/)。

---

## [Unreleased]

### 🔒 Security
- 添加 Spring Security 基础框架（ROLE_ADMIN / ROLE_USER 内存认证）
- 配置写操作需要 ADMIN 角色（/api/v1/configs/**）
- 健康检查端点允许匿名访问
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

| 特性 | v0.0.0 (原始) | Unreleased (当前) |
|------|:---:|:---:|
| LICENSE 文件 | ❌ | ✅ |
| 认证授权 | ❌ | ✅ |
| 审计日志 | ❌ | ✅ |
| 配置加密 | ❌ | ✅ |
| 测试 | ❌ | ✅ (48 tests) |
| CI/CD | ❌ | ✅ |
| K8s 部署 | ❌ | ✅ |
| Prometheus | ❌ | ✅ |
| i18n | ❌ | ✅ |
| SBOM | ❌ | ✅ |
