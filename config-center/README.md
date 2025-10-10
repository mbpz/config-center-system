# Config Center Service

分布式配置中心服务，提供配置的集中管理、动态更新、版本管理等功能。

## 项目结构

```
java/config-center/
├── pom.xml                                    # Maven 配置文件
├── docker-compose.yml                         # Docker 编排配置
├── init/                                      # 数据库初始化脚本
│   └── 01-init.sql                           # 数据库表结构
├── src/main/java/com/example/configcenter/
│   ├── ConfigCenterApplication.java          # 应用程序入口
│   ├── controller/                           # 控制器层
│   │   └── ConfigController.java            # 配置管理接口
│   ├── service/                              # 服务层
│   │   └── ConfigService.java               # 配置管理服务
│   ├── mapper/                               # 数据访问层
│   │   └── ConfigItemMapper.java            # MyBatis Mapper
│   └── entity/                               # 实体类
│       └── ConfigItem.java                  # 配置项实体
└── src/main/resources/
    └── application.yml                       # 应用配置文件
```

## 功能特性

- 配置的集中管理（CRUD操作）
- 多环境支持（开发、测试、生产）
- 配置版本控制
- 配置变更审计
- RESTful API接口
- 基于MyBatis的数据访问
- Docker容器化部署

## 技术栈

- Spring Boot 2.7.x
- MyBatis
- MySQL 8.0
- Docker & Docker Compose
- Maven

## 数据库设计

### 配置项表 (config_item)
- id: 主键
- config_key: 配置键
- config_value: 配置值
- description: 描述
- environment: 环境
- version: 版本
- status: 状态
- create_time: 创建时间
- update_time: 更新时间

## API接口

### 1. 获取配置
```
GET /api/v1/configs/{key}
参数：
- key: 配置键
- environment: 环境（默认：dev）
```

### 2. 获取环境配置列表
```
GET /api/v1/configs
参数：
- environment: 环境（默认：dev）
```

### 3. 创建配置
```
POST /api/v1/configs
请求体：
{
    "configKey": "string",
    "configValue": "string",
    "description": "string",
    "environment": "string",
    "version": "string",
    "status": "string"
}
```

### 4. 更新配置
```
PUT /api/v1/configs/{key}
参数：
- key: 配置键
请求体：同创建配置
```

### 5. 删除配置
```
DELETE /api/v1/configs/{key}
参数：
- key: 配置键
- environment: 环境（默认：dev）
```

## 构建和运行

### 环境要求
- JDK 8
- Maven 3.6+
- Docker & Docker Compose
- MySQL 8.0+

### 1. 启动数据库
```bash
# 在项目根目录下运行
docker-compose up -d
```

### 2. 构建项目
```bash
mvn clean package
```

### 3. 运行应用
```bash
mvn spring-boot:run
```

应用将在 http://localhost:8080 启动

### 4. 测试API
```bash
# 创建配置
curl -X POST http://localhost:8080/api/v1/configs \
  -H "Content-Type: application/json" \
  -d '{
    "configKey": "app.name",
    "configValue": "Config Center",
    "description": "Application name",
    "environment": "dev",
    "version": "1.0",
    "status": "ACTIVE"
  }'

# 获取配置
curl http://localhost:8080/api/v1/configs/app.name?environment=dev
```

## 配置说明

### 数据库配置
数据库连接配置在 `application.yml` 中：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/config_center
    username: config_user
    password: config123
```

### Docker配置
Docker配置在 `docker-compose.yml` 中：
- MySQL 8.0
- 端口映射：3306
- 数据持久化
- 自动初始化数据库

## 开发指南

1. 克隆项目
2. 导入IDE（推荐使用IntelliJ IDEA）
3. 安装Lombok插件
4. 配置Maven
5. 运行ConfigCenterApplication

## 注意事项

1. 生产环境部署时请修改默认密码
2. 建议启用数据库SSL连接
3. 根据实际需求调整数据库连接池配置
4. 建议使用配置中心管理应用配置 

dev
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev

mvn package
java -jar target/config-center-xxx.jar --spring.profiles.active=dev

mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev
