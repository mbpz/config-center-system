# 贡献指南

感谢你关注 Config Center System！本文档将帮助你快速上手贡献。

## 行为准则

参与本项目即表示你同意遵守我们的 [行为准则](CODE_OF_CONDUCT.md)。

## 如何贡献

### 报告 Bug

1. 先搜索 [Issues](https://github.com/mbpz/config-center-system/issues) 确认问题未被报告
2. 使用 Bug 报告模板创建 Issue
3. 提供详细的重现步骤、环境信息和日志

### 提出功能建议

1. 先搜索确认功能未被建议
2. 使用功能请求模板创建 Issue
3. 说明使用场景和期望的行为

### 提交代码

1. **Fork** 项目
2. 创建特性分支: `git checkout -b feature/my-feature`
3. 编写代码和测试
4. 确保所有测试通过: `mvn clean verify` (后端) / `npm run build` (前端)
5. 提交（遵循提交信息规范）
6. 推送到你的 Fork: `git push origin feature/my-feature`
7. 创建 Pull Request 到 `main` 分支

## 开发环境搭建

### 前置条件

- JDK 8+
- Maven 3.6+
- Node.js 16+
- Docker & Docker Compose (可选)

### 后端开发

```bash
cd config-center
cp .env.example .env
# 编辑 .env 填入配置
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev
```

API 文档: http://localhost:8080/swagger-ui.html (Phase 2)

### 前端开发

```bash
cd config-web
yarn install
npm run dev
```

前端地址: http://localhost:8000
后端代理: http://localhost:8080

### 运行测试

```bash
# 后端
cd config-center
mvn clean test

# 带覆盖率
mvn clean verify
# 报告: config-center/target/site/jacoco/index.html
```

## 提交信息规范

```
<type>(<scope>): <subject>

<body>

<footer>
```

Type:
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档变更
- `style`: 代码格式（不影响逻辑）
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具变更
- `security`: 安全相关
- `ci`: CI/CD 配置

示例:
```
feat(auth): 添加 JWT token 刷新机制

- 新增 /api/v1/auth/refresh 端点
- Token 过期时间从 1 小时延长至 24 小时
- Refresh token 有效期 7 天

Closes #42
```

## 代码规范

### Java

- 遵循 Google Java Style Guide
- 所有公开方法必须有 Javadoc
- 新增代码需要测试覆盖 (核心包 ≥ 60%)
- 使用 `@PreAuthorize` 保护敏感端点

### TypeScript/React

- 遵循 ESLint + Prettier 配置
- 组件文件使用 PascalCase
- 使用 TypeScript 严格模式
- i18n: 所有用户可见文本使用 `useLocale().t('key')`

## 版本管理

本项目遵循 [Semantic Versioning](https://semver.org/)。

- `v0.1.0`: 当前开发阶段
- `v1.0.0`: 首个稳定版本

## 安全相关

- 绝不在 Issue/PR 中泄漏真实密码或密钥
- 发现安全漏洞请私下联系维护者 (SECURITY.md)
- 涉及安全变更的 PR 请先在 Issue 中讨论

## 许可证

参与贡献即表示你的贡献将根据 [Apache License 2.0](LICENSE) 进行许可。
