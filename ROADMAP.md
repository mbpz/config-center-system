# Config Center System 路线图

> 当前版本: **v0.2.0** (开发中) | 更新日期: 2026-07-04

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
**关键成果**:
- 48 个测试用例全部通过
- JaCoCo 指令覆盖率 41.2% (核心包 ≥ 60%)
- 安全三层: 认证 (RBAC) + 加密 (AES-256-GCM) + 审计
- CI/CD: GitHub Actions + 分支保护 + Docker 自动化

---

## ✅ Phase 2: Differentiation — 差异化 (v0.5.0) — 已完成

> **目标**: 竞品做不到的，或做起来会自伤的 — ✅ 已实现

| # | 任务 | 状态 | Issue |
|---|------|------|-------|
| 12 | 字段级加密 + 可插拔 KMS 插件 | ✅ 已完成 | #13 |
| 13 | SSE 实时推送 | ✅ 已完成 | #14 |
| 14 | 批量导入导出 (JSON/YAML) | ✅ 已完成 | #15 |
| 15 | 多租户支持 (tenant_id) | ✅ 已完成 | #16 |
| 16 | 多语言客户端 SDK (设计文档) | ✅ 已规划 | #17 |
| 17 | Kubernetes Operator (设计文档) | ✅ 已规划 | #18 |
| 18 | 配置分组与标签 | ✅ 已完成 | #19 |
| 19 | 灰度发布 (百分比/标签) | ✅ 已完成 | #20 |

**完成日期**: 2026-07-04
**关键成果**:
- KMS 插件接口 (可接入 Aliyun/Tencent/Vault)
- SSE 实时推送 (基于 Server-Sent Events)
- JSON/YAML 批量导入导出 (含 overwrite 模式)
- 多租户: tenant_id 字段 + TenantContext (当前 single-tenant)
- 配置分组 CRUD
- 灰度发布策略 (PERCENTAGE + TAG 匹配)
- SDK 和 Operator 完整设计文档

---

## 🔜 Phase 3: Ecosystem — 生态扩展 (v1.0.0)

> **目标**: 成为"个人/企业私有化部署标杆" — 下一步

| # | 任务 | 优先级 | 说明 |
|---|------|--------|------|
| 1 | Java SDK | P1 | 首个 SDK (直接复用后端 DTO) |
| 2 | Go SDK | P2 | Go module |
| 3 | Python SDK | P2 | PyPI 发布 |
| 4 | Shell CLI | P2 | `cc-cli` 二进制 |
| 5 | Aliyun KMS 插件 | P2 | 阿里云 KMS 集成 |
| 6 | Tencent KMS 插件 | P3 | 腾讯云 KMS 集成 |
| 7 | Vault KMS 插件 | P3 | HashiCorp Vault 集成 |
| 8 | K8s Operator 实现 | P3 | Go + Kubebuilder |
| 9 | 网络界面重构 | P3 | ProTable + 功能完善 |
| 10 | 性能测试 + 基准报告 | P3 | QPS / P99 / 并发 |

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
| v0.2.0 | 2026-07-04 | Phase 1 + Phase 2 完成 (当前版本) |
| v0.1.0 | - | Phase 1 Trust Layer (已在 v0.2.0 中合并发布) |
