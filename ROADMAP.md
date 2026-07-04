# Config Center System 路线图

> 当前版本: **v0.3.0** (开发中) | 更新日期: 2026-07-04

---

## ✅ Phase 1: Trust Layer — 信任优先 (v0.1.0) — 已完成

> **目标**: 通过任意企业安全团队 30 分钟审查 — ✅ 已实现

| # | 任务 | 优先级 | 状态 | Issue |
|---|------|--------|------|-------|
| 1 | 添加 Apache-2.0 LICENSE + NOTICE + SPDX + SBOM | P0 | ✅ 已完成 | #2 |
| 2 | 凭证清理 + 环境变量化 | P0 | ✅ 已完成 | #3 |
| 3 | README 诚实化 + 文档重组 | P0 | ✅ 已完成 | #4 |
| 4 | Spring Security 基础框架 + 认证 | P1 | ✅ 已完成 | #5 |
| 5 | 审计日志 + /api/v1/audit | P1 | ✅ 已完成 | #6 |
| 6 | 配置值静态加密 (AES-256-GCM) | P1 | ✅ 已完成 | #7 |
| 7 | 核心路径测试覆盖 (48 tests, 41.2%) | P1 | ✅ 已完成 | #8 |
| 8 | GitHub Actions CI + Release + 分支保护 | P1 | ✅ 已完成 | #9 |
| 9 | K8s 部署清单 + Helm Chart + Prometheus | P2 | ✅ 已完成 | #10 |
| 10 | 前端脚手架清理 + i18n + ErrorBoundary | P2 | ✅ 已完成 | #11 |
| 11 | 社区脚手架 (CONTRIBUTING/COC/SECURITY) | P2 | ✅ 已完成 | #12 |

**完成日期**: 2026-07-04

---

## ✅ Phase 2: Differentiation — 差异化 (v0.5.0) — 已完成

> **目标**: 竞品做不到的，或做起来会自伤的 — ✅ 已实现

| # | 任务 | 状态 | Issue |
|---|------|------|-------|
| 12 | 字段级加密 + 可插拔 KMS 插件 | ✅ 已完成 | #13 |
| 13 | SSE 实时推送 | ✅ 已完成 | #14 |
| 14 | 批量导入导出 (JSON/YAML) | ✅ 已完成 | #15 |
| 15 | 多租户支持 (tenant_id) | ✅ 已完成 | #16 |
| 16 | 多语言客户端 SDK (设计文档) | ✅ 已完成 | #17 |
| 17 | Kubernetes Operator (设计文档) | ✅ 已完成 | #18 |
| 18 | 配置分组与标签 | ✅ 已完成 | #19 |
| 19 | 灰度发布 (百分比/标签) | ✅ 已完成 | #20 |

**完成日期**: 2026-07-04

---

## 🔜 Phase 3: Ecosystem — 生态扩展 (v1.0.0) — 进行中

> **目标**: 成为"个人/企业私有化部署标杆"

| # | 任务 | 优先级 | 状态 | 说明 |
|---|------|--------|------|------|
| 1 | Java SDK | P1 | ✅ 已完成 | `config-center-client/` (OkHttp + Jackson) |
| 2 | Shell CLI | P1 | ✅ 已完成 | `cc-cli/cc-cli.py` (Python 标准库) |
| 3 | SDK 设计规范 | P1 | ✅ 已完成 | `docs/sdk/design.md` |
| 4 | Go SDK | P2 | 📋 计划中 | Go module |
| 5 | Python SDK | P2 | 📋 计划中 | PyPI 发布 |
| 6 | Aliyun KMS 插件 | P2 | 📋 计划中 | 阿里云 KMS 集成 |
| 7 | Tencent KMS 插件 | P3 | 📋 计划中 | 腾讯云 KMS 集成 |
| 8 | Vault KMS 插件 | P3 | 📋 计划中 | HashiCorp Vault 集成 |
| 9 | K8s Operator 实现 | P3 | 📋 计划中 | Go + Kubebuilder |
| 10 | 前端界面完善 | P3 | 📋 计划中 | ProTable + 功能完善 |
| 11 | 性能测试 + 基准报告 | P3 | 📋 计划中 | QPS / P99 / 并发 |

**预计**: 2026 Q4

---

## 不做 / 已知限制 (Out of Scope)

- 不做 Apollo 级别的功能广度竞逐
- 不做 Vault 级别的 envelope encryption (v0.x)
- 不做跨数据中心同步 (未来可通过存储层实现)
- 不做完整的多租户计费体系 (Phase 3+ 可选)

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v0.3.0 | 2026-07-04 | Phase 3 首批: Java SDK + cc-cli + SDK 设计规范 |
| v0.2.0 | 2026-07-04 | Phase 1 + Phase 2 完成 |
| v0.1.0 | - | Phase 1 Trust Layer (已在 v0.2.0 中合并发布) |
