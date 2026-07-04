# 开发环境指南

## 环境要求

- JDK 8
- Maven 3.6+
- Docker & Docker Compose
- IDE (推荐使用 IntelliJ IDEA)

## 快速开始

1. 克隆项目
```bash
git clone <项目地址>
cd config-center
```

2. 启动开发环境
```bash
# 给脚本添加执行权限
chmod +x dev.sh
# 运行开发环境启动脚本
./dev.sh
```

## 数据库访问

### MySQL

1. 使用命令行访问：
```bash
# 进入MySQL容器
docker exec -it config-center-mysql mysql -u config_user -p$MYSQL_PASSWORD
# 输入密码：***

# 查看数据库
mysql> use config_center;
mysql> show tables;
mysql> select * from config_item;
```

2. 使用数据库工具访问：
- 主机：localhost
- 端口：3306
- 数据库：config_center
- 用户名：config_user
- 密码：***

### Redis

1. 使用命令行访问：
```bash
# 进入Redis容器
docker exec -it config-center-redis redis-cli -a ***

# 查看所有键
127.0.0.1:6379> keys *

# 获取特定配置
127.0.0.1:6379> get config:app.name:dev
```

2. 使用Redis工具访问：
- 主机：localhost
- 端口：6379
- 密码：***

## IDE配置

### IntelliJ IDEA

1. 导入项目
   - File -> Open -> 选择项目目录
   - 等待Maven下载依赖

2. 配置运行环境
   - 编辑运行配置
   - 添加VM选项：-Dspring.profiles.active=dev
   - 主类：com.crgmhrc.configcenter.ConfigCenterApplication

3. 配置调试
   - 在需要的地方设置断点
   - 使用Debug模式启动应用

## 常用开发命令

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f

# 停止所有服务
docker-compose down

# 清理数据（慎用）
docker-compose down -v

# 重新构建并启动
docker-compose up -d --build
```

## 调试技巧

1. 查看缓存状态
```bash
# 查看Redis中的缓存
docker exec -it config-center-redis redis-cli -a *** keys *

# 查看本地缓存统计
访问：http://localhost:8080/actuator/caches
```

2. 数据库调试
```bash
# 查看数据库日志
docker-compose logs -f mysql

# 查看慢查询
docker exec -it config-center-mysql mysql -u root -p*** -e "SHOW VARIABLES LIKE '%slow%';"
```

3. Redis调试
```bash
# 查看Redis日志
docker-compose logs -f redis

# 监控Redis命令
docker exec -it config-center-redis redis-cli -a *** monitor
```

## 常见问题

1. 端口冲突
   - MySQL端口冲突：修改docker-compose.yml中的端口映射
   - Redis端口冲突：修改docker-compose.yml中的端口映射
   - 应用端口冲突：修改application-dev.yml中的server.port

2. 连接问题
   - 检查Docker容器是否正常运行
   - 检查网络连接
   - 检查防火墙设置

3. 缓存问题
   - 清除Redis缓存：docker exec -it config-center-redis redis-cli -a *** FLUSHALL
   - 应用重启会自动清除本地缓存

## 开发建议

1. 使用开发环境配置
   - 始终使用dev profile运行
   - 避免修改生产环境配置

2. 数据库操作
   - 使用事务
   - 注意SQL性能
   - 定期备份数据

3. 缓存使用
   - 合理设置缓存时间
   - 注意缓存一致性
   - 监控缓存命中率 