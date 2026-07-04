/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * 配置值加密服务
 * 使用 AES-256-GCM 算法，每个密文附带 12 字节 IV + 16 字节 auth tag
 *
 * 密文格式 (Base64): [12字节IV][密文+16字节GCM Tag]
 *
 * 密钥来源 (按优先级):
 * 1. 环境变量 CC_MASTER_KEY (原始字符串，自动派生为 32 字节密钥)
 * 2. 启动时自动生成随机密钥 (仅开发环境，每次重启失效)
 */
@Service
public class EncryptionService {

    private static final Logger logger = LoggerFactory.getLogger(EncryptionService.class);
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int KEY_LENGTH = 32; // bytes (AES-256)

    @Value("${CC_MASTER_KEY:}")
    private String masterKeyConfig;

    private SecretKey secretKey;
    private boolean encryptionEnabled;

    @PostConstruct
    public void init() {
        if (masterKeyConfig != null && !masterKeyConfig.isEmpty()) {
            this.secretKey = deriveKey(masterKeyConfig);
            this.encryptionEnabled = true;
            logger.info("AES-256-GCM 加密已启用 (密钥来源: CC_MASTER_KEY)");
        } else {
            // 开发环境：生成随机密钥（每次重启失效，加密的值无法跨重启读取）
            byte[] randomKey = new byte[KEY_LENGTH];
            new SecureRandom().nextBytes(randomKey);
            this.secretKey = new SecretKeySpec(randomKey, ALGORITHM);
            this.encryptionEnabled = true;
            logger.warn("AES-256-GCM 加密已启用 (密钥来源: 随机生成 - 仅用于开发环境)");
        }
    }

    /**
     * 加密明文
     * @param plainText 明文
     * @return Base64 编码的密文 (IV + ciphertext + GCM tag)
     * @throws IllegalStateException 如果未配置密钥
     */
    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        if (!encryptionEnabled) {
            throw new IllegalStateException("加密服务未启用: 请设置 CC_MASTER_KEY 环境变量");
        }
        try {
            // 生成随机 IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // 加密
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // 合并 IV + ciphertext
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv);
            buffer.put(cipherText);

            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            logger.error("加密失败: {}", e.getMessage());
            throw new IllegalStateException("配置值加密失败", e);
        }
    }

    /**
     * 解密密文
     * @param encryptedText Base64 编码的密文
     * @return 明文
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null) {
            return null;
        }
        if (!encryptionEnabled) {
            throw new IllegalStateException("加密服务未启用，无法解密");
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            if (decoded.length <= GCM_IV_LENGTH) {
                throw new IllegalArgumentException("密文格式无效");
            }

            // 分离 IV 和 ciphertext
            byte[] iv = Arrays.copyOfRange(decoded, 0, GCM_IV_LENGTH);
            byte[] cipherText = Arrays.copyOfRange(decoded, GCM_IV_LENGTH, decoded.length);

            // 解密
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("解密失败: {}", e.getMessage());
            throw new IllegalStateException("配置值解密失败 (密钥不匹配或密文损坏)", e);
        }
    }

    /**
     * 判断一个字符串是否看起来像加密后的值
 * 简单启发式：长度 >= 24 且为有效 Base64 且解码后长度 >= 28 (12 IV + 16 tag + 至少 1 字节数据)
     */
    public boolean isEncrypted(String value) {
        if (value == null || value.length() < 24) {
            return false;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(value);
            return decoded.length >= (GCM_IV_LENGTH + GCM_TAG_LENGTH / 8 + 1);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 从字符串派生 32 字节密钥 (SHA-256)
     */
    private SecretKey deriveKey(String input) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha256.digest(input.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (Exception e) {
            throw new IllegalStateException("密钥派生失败", e);
        }
    }

    /**
     * 检查加密是否已启用
     */
    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }
}
