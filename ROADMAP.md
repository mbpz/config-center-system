# Config Center System 路线图

> 当前版本: v0.0.0 (pre-release) | 更新日期: 2026-07-04

---

## Phase 1: Trust Layer — 信任优先 (v0.1.0)

**目标**: 通过任意企业安全团队 30 分钟审查

| # | 任务 | 优先级 | 状态 | Issue |
|---|------|--------|------|-------|
| 1 | 添加 Apache-2.0 LICENSE + NOTICE + SPDX + SBOM | P0 | 🔴 Todo | #2 |
| 2 | 凭证清理 + 环境变量化 + 历史敏感信息轮换 | P0 | 🔴 Todo | #3 |
| 3 | README 诚实化 + ROADMAP 拆分 + 文档结构重组 | P0 | 🔴 Todo | #4 |
| 4 | Spring Security 基础框架 + 默认认证 | P1 | 🔴 Todo | #5 |
| 5 | 审计日志填充 + /api/v1/audit 端点 + 前端历史弹窗 | P1 | 🔴 Todo | #6 |
| 6 | 配置值静态加密 (AES-256-GCM) + KMS 接口 | P1 | 🔴 Todo | #7 |
| 7 | 核心路径测试覆盖 (≥60%) | P1 | 🔴 Todo | #8 |
| 8 | GitHub Actions CI + Release + 分支保护 | P1 | 🔴 Todo | #9 |
| 9 | K8s 部署清单 + Helm Chart + Prometheus 指标 | P2 | 🔴 Todo | #10 |
| 10 | 前端脚手架清理 + i18n | P2 | 🔴 Todo | #11 |
| 11 | 社区脚手架 (CONTRIBUTING/COC/SECURITY/模板) | P2 | 🔴 Todo | #12 |

**预计完成**: 2026-08-29

---

## Phase 2: Differentiation — 差异化 (v0.5.0)

**目标**: 竞品做不到的，或做起来会自伤的

| # | 任务 | 状态 | Issue |
|---|------|------|-------|
| 12 | 字段级加密 + 可插拔 KMS | 🔴 Todo | #13 |
| 13 | SSE 实时推送 | 🔴 Todo | #14 |
| 14 | 批量导入导出 (JSON/YAML) | 🔴 Todo | #15 |
| 15 | 多租户支持 | 🔴 Todo | #16 |
| 16 | 多语言客户端 SDK (Go/Python/Java/CLI) | 🔴 Todo | #17 |
| 17 | Kubernetes Operator | 🔴 Todo | #18 |
| 18 | 配置分组与标签 | 🔴 Todo | #19 |
| 19 | 灰度发布 | 🔴 Todo | #20 |

**预计完成**: 2026-11-28

---

## 不做 / 已知限制 (Out of Scope)

- 不做 Apollo 级别的功能广度竞逐
- 不做 Vault 级别的 envelope encryption (v0.x)
- 不做跨数据中心同步
- 不做完整的多租户计费体系

---

## 已完成

尚无发布版本。首个目标: v0.1.0 Trust Release。
