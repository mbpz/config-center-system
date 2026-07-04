# Config Center SDK

多语言客户端 SDK — 让任何语言的程序都能轻松接入配置中心。

## 当前状态

> 🚧 **计划阶段** — SDK 正在设计中。
> 当前版本请直接使用 REST API。

## 计划支持的 SDK

| 语言 | 包名 | 预计版本 | 状态 |
|------|------|---------|------|
| Java | `com.crgmhrc:config-center-client` | v0.2.0 | 📋 计划中 |
| Go | `github.com/mbpz/config-center-sdk-go` | v0.2.0 | 📋 计划中 |
| Python | `pip install config-center-sdk` | v0.3.0 | 📋 计划中 |
| Shell CLI | `cc-cli` 二进制 | v0.2.0 | 📋 计划中 |

## 统一 API 设计

所有 SDK 遵循相同的设计模式:

### 1. 初始化客户端

```java
// Java
ConfigClient client = ConfigClient.builder()
    .serverUrl("http://localhost:8080")
    .username("admin")
    .password("admin123")
    .environment("dev")
    .build();
```

```go
// Go
client := configcenter.NewClient(&configcenter.Config{
    ServerURL:   "http://localhost:8080",
    Username:    "admin",
    Password:    "admin123",
    Environment: "dev",
})
```

```python
# Python
client = ConfigClient(
    server_url="http://localhost:8080",
    username="admin",
    password="admin123",
    environment="dev"
)
```

### 2. 基本操作

```java
// 获取配置
ConfigItem item = client.getConfig("app.name");
String value = item.getValue();                        // 自动解密

// 获取列表 (解密后的值)
List<ConfigItem> configs = client.listConfigs("dev");

// 创建配置
client.createConfig("db.password", "secret", Map.of(
    "description", "Database password",
    "encrypted", "true"    // 启用加密存储
));

// 监听变更 (SSE)
client.watch((event) -> {
    System.out.println("配置变更: " + event.getConfigKey());
    System.out.println("新值: " + event.getNewValue());
});
```

```bash
# Shell CLI
cc-cli get app.name --env dev
cc-cli list --env dev
cc-cli set db.password secret --encrypted
cc-cli watch --env dev  # SSE 实时监听
cc-cli import configs.json --env dev
cc-cli export --env dev --format yaml > backup.yaml
```

### 3. 统一特性

所有 SDK 将支持:

- [x] 基础 CRUD (获取/创建/更新/删除配置)
- [x] 自动解密 (加密配置自动解密返回明文)
- [x] SSE 实时监听 (配置变更推送)
- [ ] 本地缓存 + 自动刷新
- [ ] 多环境切换
- [ ] 失败重试 + 超时控制
- [ ] 日志集成 (SLF4J / log / logging)
- [ ] 指标集成 (Prometheus)

## REST API 参考

在 SDK 完成之前，可以直接使用以下 API:

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | /api/v1/configs/{key}?env=dev | BASIC | 获取单个配置 |
| GET | /api/v1/configs?env=dev | BASIC | 获取列表 |
| POST | /api/v1/configs | BASIC (ADMIN) | 创建配置 |
| PUT | /api/v1/configs/{key}?env=dev | BASIC (ADMIN) | 更新配置 |
| DELETE | /api/v1/configs/{key}?env=dev | BASIC (ADMIN) | 删除配置 |
| GET | /api/v1/audit?key=&env= | BASIC | 查询审计日志 |
| GET | /api/v1/configs/export?env=&format=json\|yaml | BASIC (ADMIN) | 导出 |
| POST | /api/v1/configs/import | BASIC (ADMIN) | 导入 |
| GET | /api/v1/configs/stream | BASIC | SSE 实时流 |
| GET | /api/v1/groups | BASIC | 配置分组列表 |
| GET | /api/v1/gray-releases | BASIC | 灰度策略列表 |

## Java SDK 先行

Java SDK 将作为首个实现，因为它能直接依赖后端的 DTO 类。

### Maven 坐标 (未来)

```xml
<dependency>
    <groupId>com.crgmhrc</groupId>
    <artifactId>config-center-client</artifactId>
    <version>0.2.0</version>
</dependency>
```

## 如何贡献 SDK

如果你想贡献某个语言的 SDK:

1. 在 GitHub 创建 Issue 说明你要实现的语言和方案
2. 创建独立仓库 (如 `config-center-sdk-go`)
3. 遵循 [SDK 设计规范](design.md)
4. PR 合并后将加入官方文档

---

📧 联系: apples398@163.com
