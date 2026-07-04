/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */
package com.crgmhrc.configcenter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 配置值加密服务 (V2 - 支持 KMS 插件 + 自动加密)
 *
 * 改进:
 * 1. 支持多个 KMS Provider (Local/Aliyun/Tencent/Vault)
 * 2. 按 key 模式自动加密 (如 *.password, *.secret 自动启用)
 * 3. provider 自动路由和检测
 */
@Service
public class EncryptionService {

    private static final Logger logger = LoggerFactory.getLogger(EncryptionService.class);

    // 需要自动加密的 key 模式 (Ant-style path matching)
    @Value("${CC_ENCRYPT_PATTERNS:*.password,*.secret,*.token,*.apikey,*.private_key}")
    private String encryptPatternsConfig;

    private List<Pattern> encryptPatterns;
    private List<KmsProvider> providers;
    private KmsProvider primaryProvider;

    @Autowired(required = false)
    private List<KmsProvider> injectedProviders;

    @Autowired
    private LocalKmsProvider localKmsProvider;

    @PostConstruct
    public void init() {
        // 初始化加密模式
        this.encryptPatterns = new ArrayList<>();
        if (encryptPatternsConfig != null && !encryptPatternsConfig.isEmpty()) {
            for (String p : encryptPatternsConfig.split(",")) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty()) {
                    // Ant-style: * 匹配任意字符不含 /, ** 匹配任意
                    String regex = trimmed
                            .replace(".", "\\.")
                            .replace("**", "___DOUBLESTAR___")
                            .replace("*", "[^/]*")
                            .replace("___DOUBLESTAR___", ".*");
                    encryptPatterns.add(Pattern.compile(regex));
                }
            }
        }

        // 初始化 providers (local 始终在最后作为 fallback)
        this.providers = new ArrayList<>();
        if (injectedProviders != null) {
            for (KmsProvider p : injectedProviders) {
                if (!(p instanceof LocalKmsProvider) && p.isAvailable()) {
                    providers.add(p);
                }
            }
        }
        // local 始终作为最后一个 provider
        if (localKmsProvider.isAvailable()) {
            providers.add(localKmsProvider);
        }

        // 选择 primary provider (非 local 的第一个, 或 local 作为最后手段)
        this.primaryProvider = providers.stream()
                .filter(p -> !p.getProviderName().equals("local"))
                .findFirst()
                .orElseGet(() -> providers.isEmpty() ? null : providers.get(0));

        if (primaryProvider != null) {
            logger.info("加密服务已启用: primary={}, patterns={}, providers={}",
                    primaryProvider.getProviderName(),
                    encryptPatternsConfig,
                    providers.stream().map(KmsProvider::getProviderName).collect(Collectors.joining(",")));
        } else {
            logger.warn("加密服务未启用: 无可用 KMS Provider");
        }
    }

    /**
     * 根据 key 判断是否需要自动加密
     */
    public boolean shouldAutoEncrypt(String configKey) {
        if (configKey == null || encryptPatterns.isEmpty()) return false;
        return encryptPatterns.stream().anyMatch(p -> p.matcher(configKey).matches());
    }

    /**
     * 加密配置值 (使用 primary provider)
     */
    public String encrypt(String plainText, String environment) {
        if (plainText == null) return null;
        if (primaryProvider == null) {
            throw new EncryptionException("encrypt", "none", "无可用 KMS Provider");
        }
        KmsProvider.KmsContext ctx = new KmsProvider.KmsContext(environment);
        return primaryProvider.encrypt(plainText, ctx);
    }

    /**
     * 解密配置值 (自动检测使用哪个 provider)
     */
    public String decrypt(String cipherText, String environment) {
        if (cipherText == null) return null;
        // 尝试每个 provider
        for (KmsProvider provider : providers) {
            if (provider.canDecrypt(cipherText)) {
                try {
                    return provider.decrypt(cipherText, new KmsProvider.KmsContext(environment));
                } catch (EncryptionException e) {
                    logger.debug("Provider {} 解密失败，尝试下一个: {}",
                            provider.getProviderName(), e.getMessage());
                }
            }
        }
        throw new EncryptionException("decrypt", "unknown",
                "无可用 Provider 能解密此密文 (可能来自不同环境或密钥已轮换)");
    }

    /**
     * 判断字符串是否是此服务加密的密文
     */
    public boolean isEncrypted(String value) {
        if (value == null) return false;
        return providers.stream().anyMatch(p -> p.canDecrypt(value));
    }

    /**
     * 获取当前 primary provider 名称
     */
    public String getPrimaryProviderName() {
        return primaryProvider != null ? primaryProvider.getProviderName() : "none";
    }

    /**
     * 获取所有可用 provider 名称
     */
    public List<String> getAvailableProviders() {
        return providers.stream()
                .filter(KmsProvider::isAvailable)
                .map(KmsProvider::getProviderName)
                .collect(Collectors.toList());
    }

    /**
     * 检查加密是否已启用
     */
    public boolean isEncryptionEnabled() {
        return primaryProvider != null;
    }
}
