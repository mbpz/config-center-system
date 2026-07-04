-- 创建数据库
CREATE DATABASE IF NOT EXISTS config_center DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE config_center;

-- 创建配置项表
CREATE TABLE IF NOT EXISTS config_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT NOT NULL,
    description VARCHAR(500),
    environment VARCHAR(50) NOT NULL,
    version VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    encrypted BOOLEAN DEFAULT FALSE,
    tenant_id VARCHAR(50) NOT NULL DEFAULT 'default',
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    UNIQUE KEY uk_key_env_tenant (config_key, environment, tenant_id),
    INDEX idx_tenant (tenant_id)
);

-- 创建配置组表
CREATE TABLE IF NOT EXISTS config_group (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id VARCHAR(50) NOT NULL,
    group_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

-- 创建配置变更记录表
CREATE TABLE IF NOT EXISTS config_change_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL,
    environment VARCHAR(50) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    change_type VARCHAR(20) NOT NULL COMMENT 'CREATE/UPDATE/DELETE',
    operator VARCHAR(50) NOT NULL,
    change_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_key_env (config_key, environment),
    INDEX idx_change_time (change_time)
);

-- 灰度发布策略表
CREATE TABLE IF NOT EXISTS config_gray_release (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL,
    environment VARCHAR(50) NOT NULL,
    strategy_type VARCHAR(20) NOT NULL COMMENT 'PERCENTAGE or TAG',
    strategy_detail TEXT NOT NULL COMMENT 'JSON strategy config',
    gray_value TEXT COMMENT '灰度分流后的值',
    enabled BOOLEAN DEFAULT TRUE,
    operator VARCHAR(50) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_key_env (config_key, environment),
    INDEX idx_enabled (enabled)
);

-- Phase 2: 移除硬编码密码，应用通过环境变量配置
-- 首次部署执行: CREATE USER 'config_user'@'%' IDENTIFIED BY '随机强密码';
-- GRANT ALL PRIVILEGES ON config_center.* TO 'config_user'@'%';
-- FLUSH PRIVILEGES; 