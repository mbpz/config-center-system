# Config Center Kubernetes Operator

声明式部署和管理 Config Center 实例的 K8s Operator。

## 当前状态

> 📋 **设计阶段** — 完整的架构设计已就绪，实现将在 Phase 2 后期启动。
> 当前请使用 [Helm Chart](../../deploy/helm/config-center/) 或 [原生清单](../../deploy/kubernetes/) 部署。

## 设计目标

Config Center Operator 将提供:

1. **声明式实例管理** — 通过 `ConfigCenter` CRD 定义实例
2. **自动扩缩容** — 基于 CPU/内存/QPS 的自动水平扩展
3. **零停机升级** — 滚动更新 + 优雅关闭
4. **自动备份** — 定时快照到对象存储
5. **多租户隔离** — 一个 Operator 管理多个租户实例
6. **健康检查 + 自愈** — 自动重启不健康的 Pod

## CRD 设计

### ConfigCenter CRD

```yaml
apiVersion: configcenter.crgmhrc.com/v1alpha1
kind: ConfigCenter
metadata:
  name: production
  namespace: config-center
spec:
  version: "0.1.0"
  replicas: 3

  # 数据库配置
  database:
    host: mysql.config-center.svc
    port: 3306
    name: config_center
    credentialsSecret: mysql-creds  # 引用 K8s Secret

  # Redis 配置
  redis:
    host: redis.config-center.svc
    port: 6379
    credentialsSecret: redis-creds

  # 安全配置
  security:
    adminPasswordSecret: admin-encryption-key
    masterKeySecret: config-master-key
    corsAllowedOrigins:
      - "https://config.example.com"

  # 加密配置
  encryption:
    enabled: true
    autoEncryptPatterns:
      - "*.password"
      - "*.secret"

  # Ingress
  ingress:
    enabled: true
    host: config.example.com
    tlsSecret: config-tls-cert

  # 资源限制
  resources:
    requests:
      cpu: "200m"
      memory: "512Mi"
    limits:
      cpu: "1"
      memory: "1Gi"

  # 备份策略 (可选)
  backup:
    enabled: true
    schedule: "0 2 * * *"  # 每天凌晨 2 点
    storage:
      s3:
        bucket: config-center-backups
        region: cn-north-1
        credentialsSecret: backup-s3-creds

  # 灰度发布配置
  grayRelease:
    enabled: true
```

### 生成的资源

Operator 将根据 CRD 自动生成并管理:

- `Deployment` (带滚动更新策略)
- `Service`
- `Ingress`
- `ConfigMap` (非敏感配置)
- `HPA` (水平自动扩缩)
- `ServiceMonitor` (Prometheus 监控)
- `CronJob` (备份定时任务)

## 架构

```
┌─────────────────────────────────────────────────┐
│  Kubernetes Cluster                             │
│                                                 │
│  ┌────────────────────────────────────────────┐ │
│  │  ConfigCenter Operator (Deployment)        │ │
│  │  - Reconcile CRD                           │ │
│  │  - 管理 lifecycle                           │ │
│  │  - 触发 backup / upgrade                    │ │
│  └────────────┬───────────────────────────────┘ │
│               │ watches                          │
│  ┌────────────▼───────────────────────────────┐ │
│  │  ConfigCenter CRD                          │ │
│  │  metadata:                                 │ │
│  │    name: production                        │ │
│  │  spec:                                     │ │
│  │    replicas: 3                             │ │
│  │    database: ...                           │ │
│  │    security: ...                           │ │
│  └────────────┬───────────────────────────────┘ │
│               │ reconcile                        │
│  ┌────────────▼───────────────────────────────┐ │
│  │  Managed Resources                         │ │
│  │  - config-center-production (Deployment)   │ │
│  │  - config-center-production (Service)      │ │
│  │  - config-center-production (Ingress)      │ │
│  │  - config-center-production (HPA)          │ │
│  └────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

## 技术栈

- **语言**: Go (Kubebuilder / Operator SDK)
- **API**: Kubernetes API Server (CRD)
- **框架**: [Kubebuilder](https://kubebuilder.io/) v3
- **镜像仓库**: ghcr.io/mbpz/config-center-operator

## 实现路线图

### Phase 2 后期 (计划中)

- [ ] CRD 定义 (api/v1alpha1/configcenter_types.go)
- [ ] Controller 实现 (configcenter_controller.go)
- [ ] 基础 Reconcile 逻辑 (Deployment/Service/ConfigMap)
- [ ] Webhook (验证 + 默认值)
- [ ] 集成测试 (envtest)
- [ ] 文档和示例

### Phase 3 (未来)

- [ ] 自动备份 CronJob
- [ ] 零停机升级
- [ ] 多租户 CRD (按 namespace 隔离)
- [ ] Prometheus 指标导出 (Operator 自身监控)
- [ ] Helm Chart for Operator 部署
- [ ] OLM (Operator Lifecycle Manager) 集成

## 使用 Helm 部署 (替代方案)

在 Operator 完成之前，使用 Helm Chart 部署:

```bash
helm install config-center ./deploy/helm/config-center \
  --set image.tag=latest \
  --set config.datasource.host=mysql.svc \
  --set config.security.adminPassword=changeme
```

## 贡献

Operator 是一个独立项目。如果你想参与:

1. 查看 [设计文档](design.md)
2. 在 Issue 中讨论方案
3. PR 欢迎!

---

📧 联系: apples398@163.com
