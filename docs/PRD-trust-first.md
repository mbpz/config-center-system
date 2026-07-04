# PRD: Config Center System — Trust-First 信任优先改造

> **文档状态**: v0.1 Draft | **创建日期**: 2026-07-04 | **作者**: mbpz
> **来源**: Council of High Intelligence 审议裁决（策略三巨头：孙子/马基雅维利/奥勒留）
> **目标**: 将 config-center-system 从"功能原型"改造为"成熟的开源项目 + 个人/企业私有化部署标杆"

---

## 1. 背景与问题

### 1.1 当前状态

config-center-system 是一个完整但基础的分布式配置中心：
- **后端**: Spring Boot 2.7.18 + MyBatis + MySQL 8.0 + Redis 7.0 + Caffeine
- **前端**: React 18 + UmiJS 4 + Ant Design 5 + TypeScript
- **已有能力**: 配置 CRUD、多环境（dev/test/prod）、版本字段、三级缓存（Caffeine/Redis/MySQL）、Redis 降级、Docker 部署
- **包名**: `com.crgmhrc.configcenter` | **仓库**: `github.com/mbpz/config-center-system`

### 1.2 审计报告摘要（代码 vs README 差距）

| README 声称 | 实际代码 | 严重度 |
|---|---|---|
| SSO 认证 | 零安全实现 | 🔴 致命 |
| RBAC 权限控制 | access.ts 是静态桩 `return { name: '配置中心' }` | 🔴 致命 |
| 敏感配置加密存储 | `config_value` 明文 TEXT，无 Jasypt/AES | 🔴 致命 |
| 版本回滚 | `config_change_log` 表存在但无 Java 代码读写 | 🔴 致命 |
| 批量导入导出 | 无任何 import/export 端点 | 🟡 高 |
| 实时推送 | 无 WebSocket/SSE/long-poll，仅轮询 | 🟡 高 |
| K8s HA ≥2 节点 | 单实例 docker-compose，无 k8s manifest | 🔴 致命 |
| 99.9% 可用性 | 无测试、无指标、无基准 | 🟡 高 |
| MIT 许可证 | 声称 MIT，无 LICENSE 文件 | 🔴 致命 |

### 1.3 代码审计发现的关键缺陷

1. **真实凭证提交到 git**: `application.yml` 中 `password: mysql_ZJHZsF`、内网 IP `192.168.3.181`
2. **零认证授权**: ConfigController 完全开放，无 Spring Security，无 filter/interceptor
3. **配置值明文存储**: `useSSL=false`，无 Jasypt，无 KMS
4. **审计表从未写入**: `config_change_log` 有 schema 无 mapper
5. **无测试**: `config-center/src/test` 不存在，`spring-boot-starter-test` 是死依赖
6. **无 CI/CD**: 无 `.github/workflows`，仅有 shell 脚本
7. **脚手架页面当产品**: `Access/`、`Table/` 是 UmiJS 默认模板，含 `@ts-ignore` 和 mock 数据

---

## 2. 战略定位

### 2.1 核心定位

> **"5 分钟部署、默认加密、全部可审计、插拔式 KMS 的配置中心"**

不是比 Apollo/Nacos 功能多，是比它们**更轻、更安全、更诚实**。

### 2.2 目标用户画像

| 用户 | 场景 | 核心需求 |
|---|---|---|
| 独立开发者 / 小团队 | 个人项目私有化部署 | 单二进制、零依赖、一行 Docker 启动 |
| 中型企业 DevOps | 内部多服务配置集中管理 | 安全合规（审计、加密、RBAC）、K8s 部署 |
| 安全守门人 | 采购/技术审批 | LICENSE、SBOM、威胁模型、合规证明 |

### 2.3 差异化定位（竞品对比）

| 维度 | Apollo | Nacos | Spring Cloud Config | **本项目** |
|---|---|---|---|---|
| 部署复杂度 | 高（多服务+ Eureka） | 中 | 低 | **最低**（单 binary） |
| 默认加密 | ❌ | ❌ | ❌ | **✅** |
| 开箱审计 | ❌ | ❌ | ❌ | **✅** |
| K8s 原生 | ❌ | 部分 | ❌ | **✅** |
| 中文社区 | ✅✅ | ✅ | ❌ | ✅ |
| 功能广度 | ✅✅ | ✅ | ✅ | ❌ |

---

## 3. 愿景与里程碑

### 3.1 北极星指标
- **v0.1.0 (Trust Release)**: 通过任意企业安全团队 30 分钟审查
- **v0.5.0 (Community Release)**: 100+ GitHub stars，3+ 外部贡献者
- **v1.0.0 (Benchmark Release)**: 被引用为"最易部署的私有配置中心"

### 3.2 否决条件（Kill Criteria）
1. 90 天内无 LICENSE → 停止开源主张
2. 60 天内 README 仍列不存在的特性 → 重写或放弃
3. 6 个月内无一次生产部署 → "标杆"主张失败
4. 任何关键 CVE 超 30 天未修补 → 作废"安全优先"叙事

---

## 4. Phase 1 — 信任层（第 1-8 周）

> **原则**: 无新功能。只做让企业安全守门人说"可以"的最小改动集。

### Epic 1.1: 许可证与法律合规

**目标**: 法务部门不再一票否决。

**验收标准**:
- [ ] 仓库根目录存在 `LICENSE`（Apache-2.0）
- [ ] 仓库根目录存在 `NOTICE`
- [ ] 每个 Java/TS 源文件头包含 SPDX 标识 (`SPDX-License-Identifier: Apache-2.0`)
- [ ] 生成 `SBOM`（`target/sbom.spdx.json` 或 `package.json` 同级）
- [ ] README 许可证章节指向实际文件

**Issue**: #101

---

### Epic 1.2: 凭证清理与安全加固

**目标**: git 历史中不再有真实凭证，运行时不硬编码任何密码。

**验收标准**:
- [ ] `.env`、`application.yml`、`application-dev.yml`、`docker-compose*.yml`、`deploy-remote.sh` 中无真实密码
- [ ] 真实密码已轮换（MySQL/Redis）
- [ ] 新增 `.env.example` 作为模板
- [ ] `.gitignore` 包含 `.env`
- [ ] `application.yml` 所有密码读取自环境变量（`${MYSQL_PASSWORD:default_dev_password}`）
- [ ] `useSSL=true` 或可配置 `db.ssl-mode=REQUIRED`
- [ ] `deploy-remote.sh` 使用 SSH key + env 文件，不内嵌密码
- [ ] Docker healthcheck 不在 CLI 暴露密码（改用 ` MYSQL-PWD` 文件引用或 Redis `--no-auth-warning` + 专用探针用户）

**Issue**: #102

---

### Epic 1.3: README 与文档诚实化

**目标**: README 宣称的每一行都在代码中可验证。

**验收标准**:
- [ ] 所有未实现特性（SSO/RBAC/K8s HA/批量导入导出/推送/加密）从 README 移除或移至 `ROADMAP.md` 并在主 README 标注 "🚧 Planned"
- [ ] README 章节顺序重排: 演示 → 安装 → 配置 → 架构 → 路线图 → 贡献
- [ ] 添加"当前状态"徽章（build、coverage、Docker pulls）
- [ ] 添加"快速开始"单行 Docker 命令
- [ ] `docs/design.md` (design.md vs as-built) 添加"已实现 / 未实现"双栏对比表
- [ ] 前端 README (`config-web/README.md`) 更新，不再引用旧包名 `com.example.configcenter`

**Issue**: #103

---

### Epic 1.4: Spring Security 基础框架

**目标**: 默认安全，配置变更必须有认证。

**用户故事**:
- 作为运维，我希望默认启用基本认证（admin/bootstrap token），而不是裸奔
- 作为安全审查者，我看到 Spring Security 依赖和默认配置就能信任

**验收标准**:
- [ ] `pom.xml` 引入 `spring-boot-starter-security`
- [ ] 新增 `SecurityConfig.java`，默认:
  - `/api/v1/configs/**` 写操作需要 `ROLE_ADMIN`
  - `/api/v1/configs/**` 读操作可配置为允许匿名（默认不开放）
  - `/api/v1/health/**` 允许匿名
  - CSRF 关闭（API-only, token-based）
  - CORS 可配置
- [ ] 新增 `application.yml` security 段，允许环境变量覆盖用户/密码
- [ ] 新增 bootstrap token 机制（首次启动随机生成，打印到日志）
- [ ] 前端 `access.ts` 接入真实认证状态（调用 `/api/v1/auth/me`）
- [ ] 删除前端 `Access/index.tsx` 占位页面（替换为真实权限管理 UI 或暂时移除路由）

**Issue**: #104

---

### Epic 1.5: 审计日志填充

**目标**: 每次配置变更可追溯——谁、何时、改了什么。

**用户故事**:
- 作为安全守门人，我能查看某个 key 的完整变更历史
- 作为审计员，我无法篡改已记录的变更

**验收标准**:
- [ ] `ConfigService.create/update/delete` 方法每次写操作同步插入 `config_change_log`
- [ ] `config_change_log` 字段填充: `operator`（来自 security context / `"bootstrap"`）、`old_value`、`new_value`、`change_time`
- [ ] 新增 `AuditController` 暴露 `GET /api/v1/audit?key=&env=&from=&to=`
- [ ] 新增 `AuditMapper` Java 接口
- [ ] 审计写入失败不阻塞主操作（异步 + 降级 warn log）
- [ ] 前端 Config 页面"历史"按钮打开变更记录弹窗
- [ ] `operator` 字段列为必填（security context 取 username，未登录为 `system`）

**Issue**: #105

---

### Epic 1.6: 配置值加密（静态加密）

**目标**: 数据库泄露不等于配置泄露。

**用户故事**:
- 作为 DevOps，我能标记一个 key 为 `sensitive`，其值在 DB 中加密存储
- 作为攻击者，即使拖库也拿不到明文密码/token

**验收标准**:
- [ ] 新增 `ConfigItem.encrypted` 字段（BOOLEAN, default false）
- [ ] 新增 `EncryptionService`，支持 AES-256-GCM
- [ ] 密钥来源可配置: env `CC_MASTER_KEY` 或 Java KeyStore
- [ ] 写入时: `encrypted=true` 的 value 加密后写入
- [ ] 读取时: 解密后返回，但 `/api/v1/audit` 中 old/new 显示为 `***`
- [ ] 加密失败时（密钥缺失、降级）明确报错，不静默存明文
- [ ] 公开文档解释加密方案（算法、密钥轮换、已知限制）
- [ ] 不引入 Vault 级别的 envelope encryption（v0.x 作为够用）

**Issue**: #106

---

### Epic 1.7: 测试覆盖

**目标**: 核心路径有回归保护。

**验收标准**:
- [ ] `config-center/src/test/java` 存在以下测试:
  - `ConfigServiceTest`: CRUD + 缓存命中/失效
  - `CacheServiceFallbackTest`: Redis 宕机 → 降级 → 恢复
  - `EncryptionServiceTest`: 加解密 roundtrip + 密文 ≠ 明文
  - `SecurityConfigTest`: 未认证 401 / 无角色 403 / 有角色 200
- [ ] `config-web/src` 存在 React Testing Library 测试覆盖 `Config/index.tsx` 主路径
- [ ] `pom.xml` 引入测试覆盖率插件（JaCoCo 或 surefire）
- [ ] 覆盖率门槛: core package ≥ 60%
- [ ] `README` 添加 coverage badge

**Issue**: #107

---

### Epic 1.8: CI/CD Pipeline

**目标**: 每次 PR 自动验证，每次 release 自动出包。

**验收标准**:
- [ ] `.github/workflows/ci.yml`:
  - PR 触发: maven test + frontend lint + build
  - main 合并触发: build + test + Docker image 构建
  - 使用 `mysql` + `redis` service container 做集成测试
- [ ] `.github/workflows/release.yml`:
  - Tag `v*` 触发: build + push Docker image + draft release notes
- [ ] 引入 `semantic-release` 或手动 CHANGELOG 流程
- [ ] 添加 `.github/PULL_REQUEST_TEMPLATE.md`
- [ ] 添加 `.github/ISSUE_TEMPLATE/bug_report.md` + `feature_request.md`
- [ ] main 分支保护: 不能直接 push，需要 PR + CI green

**Issue**: #108

---

### Epic 1.9: K8s 与可观测性

**目标**: DevOps 团队一键部署 + 生产可见。

**验收标准**:
- [ ] `deploy/kubernetes/` 目录:
  - `deployment.yaml`（含 liveness/readiness + resource limits）
  - `service.yaml`
  - `configmap.yaml`
  - `secret.yaml`（模板，不含真实值）
  - `ingress.yaml`（可选）
  - `hpa.yaml`（可选）
- [ ] `deploy/helm/config-center/Chart.yaml`（Helm chart 骨架）
- [ ] Prometheus metrics（micrometer）:
  - `config_read_seconds` (Timer, p50/p95/p99)
  - `cache_hit_total` / `cache_miss_total`（按 layer 标签）
  - `redis_available`（0/1 gauge）
- [ ] 暴露 `/actuator/prometheus`（引入 `spring-boot-starter-actuator` + micrometer-registry-prometheus）
- [ ] 结构化 JSON 日志（`logback-spring.xml` + logstash-logback-encoder）
- [ ] `DEVELOPMENT.md` 修复 `/actuator/caches` 引用（或引入 actuator）

**Issue**: #109

---

### Epic 1.10: 脚手架清理与前端整治

**目标**: 前端不再有 UmiJS 默认模板页面。

**验收标准**:
- [ ] 删除 `src/pages/Home`, `src/pages/Access`, `src/pages/Table` 占位页面
- [ ] 保留: `Config/`（核心 CRUD）、`Cache/`（监控）
- [ ] 重写 `src/app.ts` `getInitialState()` 调用真实 `/api/v1/auth/me`
- [ ] 重写 `src/access.ts` 基于后端返回权限
- [ ] 路由守卫: 未登录跳转 `/login`，无权限显示 403
- [ ] 新增 `/login` 页面（bootstrap token 输入）
- [ ] 前端 i18n 框架（至少中/英双语 + antd ConfigProvider locale 切换）
- [ ] 删除 `@ts-ignore` 残留
- [ ] 前端路由 lazy loading + 错误边界

**Issue**: #110

---

### Epic 1.11: 社区脚手架

**目标**: 让"想要贡献的人"30 分钟上手。

**验收标准**:
- [ ] `CONTRIBUTING.md` 包含: 行为准则引用、开发环境、PR 流程、提交信息规范
- [ ] `CODE_OF_CONDUCT.md`
- [ ] `SECURITY.md`（漏洞披露流程 + 威胁模型链接）
- [ ] `ROADMAP.md`（Phase 1 8 个 Epic + Phase 2 计划）
- [ ] `CHANGELOG.md`（从 v0.0.0 起真实记录，每次 release 更新）
- [ ] Issue templates: bug / feature request / config question
- [ ] PR template
- [ ] README 顶部添加 badges（build、coverage、Docker、license）

**Issue**: #111

---

## 5. Phase 2 — 差异化（第 9-20 周）

> **原则**: 竞品做不到的，或做起来会自伤的。每个特性都有清晰的市场定位。

### Epic 2.1: 字段级加密 + 可插拔 KMS
- 支持按 key 后缀（如 `*.password`, `*.secret`）自动加密
- KMS 插件接口: 本地 AES / 阿里 KMS / 腾讯 KMS / HashiCorp Vault
- 不引入 Vault 级 envelope；v0.x 用单主密钥 + 轮换

### Epic 2.2: SSE 实时推送
- `GET /api/v1/configs/stream` → Server-Sent Events
- 后端用 `@TransactionalEventListener` 监听配置变更事件
- 前端 Config 页面 subscribe → 自动刷新表格
- 设计文档中已承诺（Mermaid 时序图已画）

### Epic 2.3: 批量导入导出
- `POST /api/v1/configs/import` (multipart JSON/YAML)
- `GET /api/v1/configs/export?env=dev` (JSON/YAML)
- 前端"导入/导出"按钮

### Epic 2.4: 多租户
- `config_item` 加 `tenant_id`
- API filter 强制按 tenant 隔离
- cache key 加 tenant 前缀
- 初始只支持 single-tenant 模式简化部署，tenant_id 字段预留

### Epic 2.5: 多语言客户端 SDK
- Go SDK (Go module)
- Python SDK (PyPI package)
- Java Client (Maven，独立 SDK 模块)
- Shell CLI (`cc-cli` 二进制)

### Epic 2.6: K8s Operator
- CRD `ConfigCenter` 声明式部署
- Operator 处理 configmap/secret → DB 同步
- 自动滚动更新

### Epic 2.7: 配置分组与标签
- `config_group` 表已有 schema，补全 controller/service/mapper
- 前端按分组/标签过滤

### Epic 2.8: 灰度发布
- 配置值按标签/权重分流
- 前端灰度策略配置 UI

---

## 6. 度量与验收

### 6.1 Phase 1 完成定义 (DoD)

| 指标 | 目标 |
|---|---|
| LICENSE 文件存在 | ✅ |
| CI 通过率 | 100% (PR + main) |
| 测试覆盖率 (core) | ≥ 60% |
| Docker 镜像发布 | 自动推送到 ghcr.io |
| Helm chart 可用 | helm install 一键部署 |
| README vs 代码一致性 | 零未实现特性声称 |
| 安全基线扫描 | 无 CRITICAL 漏洞 |
| 审计日志覆盖率 | 100% 配置写操作 |

### 6.2 验收集弃点

| 时间 | 检查项 | 失败动作 |
|---|---|---|
| W4 | LICENSE + README 诚实 + 凭证清理 | 暂停其他工作，优先这三件 |
| W8 | CI + 测试 + Security + 审计 | 不进入 Phase 2 |
| W12 | K8s + 可观测 + 社区脚手架 | 不做"标杆"宣传 |
| W20 | Phase 2 至少完成 Epic 2.1-2.3 | 重新评估定位 |

---

## 7. 工作拆分与优先级

```
P0 (本周)     : #101 LICENSE, #102 凭证清理, #103 README
P1 (W2-W4)    : #104 Spring Security, #105 审计, #106 加密
P2 (W5-W8)    : #107 测试, #108 CI/CD, #109 K8s+可观测
P3 (W9-W12)   : #110 前端整治, #111 社区
P4 (W13-W20)  : Phase 2 Epic 2.1-2.8
```

---

## 8. 风险与对策

| 风险 | 概率 | 对策 |
|---|---|---|
| 创始人带宽不足，Phase 1 拖延 | 高 | 先做 P0 三件事（1 周内可完成），再逐步推进 |
| 竞品 Nacos 先做了加密 | 中 | 定位差异化：单 binary + 可插拔 KMS + 审计即服务 |
| README 诚实化被当做"能力缩水" | 低 | 强调"诚实 = 信任 = 长期采用"，ROADMAP 展示能力方向 |
| Spring Security 与现有 API 兼容性 | 中 | 灰度上线：admin token → RBAC 两步走 |
| 凭证轮换后旧 commit 仍在 git 历史 | 中 | git filter-repo 清除敏感文件历史（或仅轮换凭证 + gitignore 防护增量） |

---

## 9. 参考资料

- 竞品: [Apollo](https://github.com/apolloconfig/apollo) · [Nacos](https://github.com/alibaba/nacos) · [Spring Cloud Config](https://spring.io/projects/spring-cloud-config)
- 安全基线: [OWASP Config Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Configuration_Cheat_Sheet.html)
- 许可证选择: Apache-2.0 (企业友好，允许商用，专利授权)
- SBOM 工具: `cyclonedx-maven-plugin` / `sbom-generator`
- Helm 入门: [helm.sh/docs](https://helm.sh/docs/)

---

## 10. 变更记录

| 版本 | 日期 | 变更 | 作者 |
|---|---|---|---|
| v0.1 | 2026-07-04 | 初稿，基于 Council 审议裁决 | mbpz |
