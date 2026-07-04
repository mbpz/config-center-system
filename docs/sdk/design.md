# SDK 设计规范

## 命名规范

所有 SDK 遵循统一的命名和 API 设计模式:

### 客户端初始化

所有 SDK 使用 Builder 模式:

```java
// Java
ConfigClient.builder()
    .serverUrl("http://localhost:8080")
    .username("admin").password("admin123")
    .environment("dev").build();
```

```go
// Go
client := configcenter.New(
    configcenter.WithServerURL("http://localhost:8080"),
    configcenter.WithAuth("admin", "admin123"),
    configcenter.WithEnvironment("dev"),
)
```

```python
# Python
client = ConfigClient(
    server_url="http://localhost:8080",
    username="admin", password="admin123",
    environment="dev",
)
```

### 统一方法名

| 操作 | Java | Go | Python | CLI |
|------|------|-----|--------|-----|
| 获取单个 | `getConfig(key)` | `GetConfig(key)` | `get_config(key)` | `cc-cli get <key>` |
| 列表 | `listConfigs(env)` | `ListConfigs(env)` | `list_configs(env)` | `cc-cli list --env dev` |
| 创建 | `createConfig(item)` | `CreateConfig(item)` | `create_config(item)` | `cc-cli set <key> <value>` |
| 更新 | `updateConfig(key, item)` | `UpdateConfig(key, item)` | `update_config(key, item)` | `cc-cli update <key> <value>` |
| 删除 | `deleteConfig(key)` | `DeleteConfig(key)` | `delete_config(key)` | `cc-cli delete <key>` |
| 导入 | - | - | - | `cc-cli import <file>` |
| 导出 | - | - | - | `cc-cli export --format yaml` |
| 监听 | `watch(callback)` | `Watch(ctx, callback)` | `watch(callback)` | `cc-cli watch` |

### 统一错误类型

| HTTP 状态码 | Java | Go | Python |
|-----------|------|-----|--------|
| 401 | `AuthenticationError` | `ErrAuth` | `AuthenticationError` |
| 403 | `AuthorizationError` | `ErrForbidden` | `PermissionError` |
| 404 | `NotFoundError` | `ErrNotFound` | `NotFoundError` |
| 500 | `ServerError` | `ErrInternal` | `ServerError` |
| Network | `NetworkError` | `ErrNetwork` | `ConnectionError` |

## 认证方式

当前仅支持 HTTP Basic Auth。后续将支持:
- JWT Token
- API Key

## 重试策略

- 默认重试 3 次 (仅对 5xx 和网络错误)
- 退避算法: 指数退避 (1s, 2s, 4s)
- 不重试: 401, 403, 404

## SSE 监听

所有 SDK 支持 Server-Sent Events 实时监听配置变更:

```java
client.watch(event -> {
    System.out.println("变更: " + event.getType() + " " + event.getConfigKey());
});
```

## 本地缓存 (未来)

可选的本地缓存层:
- TTL 刷新
- 离线读取
- 变更时自动失效
