-- Create database if not exists
CREATE DATABASE IF NOT EXISTS config_center;
USE config_center;

-- 配置项表
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

-- 配置组表
CREATE TABLE IF NOT EXISTS config_group (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id VARCHAR(50) NOT NULL,
    group_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

-- 配置变更记录表
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

-- Grant privileges to config_user
GRANT ALL PRIVILEGES ON config_center.* TO 'config_user'@'%';
FLUSH PRIVILEGES; 