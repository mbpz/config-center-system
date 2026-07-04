/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */
package com.crgmhrc.configcenter.config;

/**
 * KMS (Key Management Service) 插件接口
 *
 * 允许接入不同的密钥管理系统:
 * - LocalKmsProvider: 本地 AES-256-GCM (默认, 适合个人/小团队)
 * - AliyunKmsProvider: 阿里云 KMS (适合阿里云用户)
 * - TencentKmsProvider: 腾讯云 KMS (适合腾讯云用户)
 * - VaultKmsProvider: HashiCorp Vault (适合多云/企业级)
 *
 * 实现要求:
 * - 所有方法必须线程安全
 * - encrypt/decrypt 失败时应抛出 EncryptionException (非运行时异常)
 * - providerName 必须唯一，用于标识和管理
 */
public interface KmsProvider {

    /**
     * 获取 Provider 名称标识
     * 例如: "local", "aliyun", "tencent", "vault"
     */
    String getProviderName();

    /**
     * 加密明文
     *
     * @param plainText 明文数据
     * @param context 加密上下文 (可选，包含 key ID 等额外信息)
     * @return 密文 (格式由 provider 自定义，但必须可逆)
     * @throws com.crgmhrc.configcenter.config.EncryptionException 加密失败
     */
    String encrypt(String plainText, KmsContext context);

    /**
     * 解密密文
     *
     * @param cipherText encrypt() 返回的密文
     * @param context 解密上下文
     * @return 明文
     * @throws com.crgmhrc.configcenter.config.EncryptionException 解密失败
     */
    String decrypt(String cipherText, KmsContext context);

    /**
     * 判断字符串是否由此 Provider 加密
     * 用于自动检测密文归属哪个 provider
     */
    boolean canDecrypt(String cipherText);

    /**
     * 检查 Provider 是否可用 (配置正确且可连通)
     */
    boolean isAvailable();

    /**
     * 加密上下文 - 传递给 provider 的额外信息
     */
    class KmsContext {
        private String keyId;
        private String environment;
        private java.util.Map<String, String> extras;

        public KmsContext() {
            this.extras = new java.util.HashMap<>();
        }

        public KmsContext(String environment) {
            this();
            this.environment = environment;
        }

        // Getters and Setters
        public String getKeyId() { return keyId; }
        public void setKeyId(String keyId) { this.keyId = keyId; }
        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }
        public java.util.Map<String, String> getExtras() { return extras; }
        public void putExtra(String key, String value) { this.extras.put(key, value); }
        public String getExtra(String key) { return this.extras.get(key); }
    }
}
