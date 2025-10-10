-- 插入测试数据
USE config_center;

-- 清空现有数据，避免重复键错误
DELETE FROM config_item;
DELETE FROM config_group;

-- 插入开发环境配置
INSERT INTO config_item (config_key, config_value, description, environment, version, status, create_time, update_time) VALUES
('app.name', 'Config Center', '应用名称', 'dev', '1.0.0', 'ACTIVE', NOW(), NOW()),
('app.version', '1.0.0', '应用版本', 'dev', '1.0.0', 'ACTIVE', NOW(), NOW()),
('server.port', '8080', '服务器端口', 'dev', '1.0.0', 'ACTIVE', NOW(), NOW()),
('spring.datasource.url', 'jdbc:mysql://192.168.3.181:3306/config_center', '数据库连接URL', 'dev', '1.0.0', 'ACTIVE', NOW(), NOW()),
('spring.redis.host', '192.168.3.181', 'Redis主机地址', 'dev', '1.0.0', 'ACTIVE', NOW(), NOW()),
('spring.redis.port', '6379', 'Redis端口', 'dev', '1.0.0', 'ACTIVE', NOW(), NOW()),
('logging.level.root', 'INFO', '根日志级别', 'dev', '1.0.0', 'ACTIVE', NOW(), NOW()),
('logging.level.com.crgmhrc.configcenter', 'DEBUG', '应用包日志级别', 'dev', '1.0.0', 'ACTIVE', NOW(), NOW()),
('cache.ttl', '3600', '缓存过期时间（秒）', 'dev', '1.0.0', 'ACTIVE', NOW(), NOW()),
('feature.redis.enabled', 'true', 'Redis功能开关', 'dev', '1.0.0', 'ACTIVE', NOW(), NOW());

-- 插入测试环境配置
INSERT INTO config_item (config_key, config_value, description, environment, version, status, create_time, update_time) VALUES
('app.name', 'Config Center Test', '应用名称', 'test', '1.0.0', 'ACTIVE', NOW(), NOW()),
('app.version', '1.0.0', '应用版本', 'test', '1.0.0', 'ACTIVE', NOW(), NOW()),
('server.port', '8081', '服务器端口', 'test', '1.0.0', 'ACTIVE', NOW(), NOW()),
('spring.datasource.url', 'jdbc:mysql://192.168.3.181:3306/config_center_test', '数据库连接URL', 'test', '1.0.0', 'ACTIVE', NOW(), NOW()),
('spring.redis.host', '192.168.3.181', 'Redis主机地址', 'test', '1.0.0', 'ACTIVE', NOW(), NOW()),
('spring.redis.port', '6379', 'Redis端口', 'test', '1.0.0', 'ACTIVE', NOW(), NOW()),
('logging.level.root', 'WARN', '根日志级别', 'test', '1.0.0', 'ACTIVE', NOW(), NOW()),
('logging.level.com.crgmhrc.configcenter', 'INFO', '应用包日志级别', 'test', '1.0.0', 'ACTIVE', NOW(), NOW()),
('cache.ttl', '1800', '缓存过期时间（秒）', 'test', '1.0.0', 'ACTIVE', NOW(), NOW()),
('feature.redis.enabled', 'true', 'Redis功能开关', 'test', '1.0.0', 'ACTIVE', NOW(), NOW());

-- 插入生产环境配置
INSERT INTO config_item (config_key, config_value, description, environment, version, status, create_time, update_time) VALUES
('app.name', 'Config Center Prod', '应用名称', 'prod', '1.0.0', 'ACTIVE', NOW(), NOW()),
('app.version', '1.0.0', '应用版本', 'prod', '1.0.0', 'ACTIVE', NOW(), NOW()),
('server.port', '8080', '服务器端口', 'prod', '1.0.0', 'ACTIVE', NOW(), NOW()),
('spring.datasource.url', 'jdbc:mysql://192.168.3.181:3306/config_center_prod', '数据库连接URL', 'prod', '1.0.0', 'ACTIVE', NOW(), NOW()),
('spring.redis.host', '192.168.3.181', 'Redis主机地址', 'prod', '1.0.0', 'ACTIVE', NOW(), NOW()),
('spring.redis.port', '6379', 'Redis端口', 'prod', '1.0.0', 'ACTIVE', NOW(), NOW()),
('logging.level.root', 'ERROR', '根日志级别', 'prod', '1.0.0', 'ACTIVE', NOW(), NOW()),
('logging.level.com.crgmhrc.configcenter', 'WARN', '应用包日志级别', 'prod', '1.0.0', 'ACTIVE', NOW(), NOW()),
('cache.ttl', '7200', '缓存过期时间（秒）', 'prod', '1.0.0', 'ACTIVE', NOW(), NOW()),
('feature.redis.enabled', 'true', 'Redis功能开关', 'prod', '1.0.0', 'ACTIVE', NOW(), NOW());

-- 插入配置组数据
INSERT INTO config_group (group_id, group_name, description, create_time, update_time) VALUES
('APP_CONFIG', '应用配置', '应用基础配置组', NOW(), NOW()),
('DATABASE_CONFIG', '数据库配置', '数据库连接配置组', NOW(), NOW()),
('REDIS_CONFIG', 'Redis配置', 'Redis缓存配置组', NOW(), NOW()),
('LOGGING_CONFIG', '日志配置', '日志级别配置组', NOW(), NOW()),
('CACHE_CONFIG', '缓存配置', '缓存相关配置组', NOW(), NOW()),
('FEATURE_CONFIG', '功能开关', '功能开关配置组', NOW(), NOW()); 