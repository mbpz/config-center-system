/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */
package com.crgmhrc.configcenter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
import java.util.regex.Pattern;

/**
 * 本地 KMS Provider
 * 使用 AES-256-GCM 算法，密钥由本地主密钥派生
 *
 * 适用于: 个人项目、小团队、开发环境
 * 不适合: 企业级生产 (建议使用 AliyunKms / TencentKms / VaultKms)
 */
@Component
public class LocalKmsProvider implements KmsProvider {

    private static final Logger logger = LoggerFactory.getLogger(LocalKmsProvider.class);

    private static final String PROVIDER_NAME = "local";
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int KEY_LENGTH = 32; // bytes (AES-256)

    // 密文前缀标识，用于自动检测密文归属
    private static final String CIPHER_PREFIX = "$local$";
    private static final Pattern ENCRYPTED_PATTERN = Pattern.compile("^\\$local\\$.+");

    @Value("${CC_MASTER_KEY:}")
    private String masterKeyConfig;

    private SecretKey secretKey;
    private boolean available;

    @PostConstruct
    public void init() {
        if (masterKeyConfig != null && !masterKeyConfig.isEmpty()) {
            this.secretKey = deriveKey(masterKeyConfig);
            this.available = true;
            logger.info("LocalKmsProvider 已启用 (密钥来源: CC_MASTER_KEY)");
        } else {
            // 开发环境生成随机密钥
            byte[] randomKey = new byte[KEY_LENGTH];
            new SecureRandom().nextBytes(randomKey);
            this.secretKey = new SecretKeySpec(randomKey, ALGORITHM);
            this.available = true;
            logger.warn("LocalKmsProvider 已启用 (随机密钥 - 仅开发环境使用!)");
        }
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public String encrypt(String plainText, KmsContext context) {
        if (plainText == null) return null;
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherBytes.length);
            buffer.put(iv);
            buffer.put(cipherBytes);

            return CIPHER_PREFIX + Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new EncryptionException("encrypt", PROVIDER_NAME, e.getMessage(), e);
        }
    }

    @Override
    public String decrypt(String cipherText, KmsContext context) {
        if (cipherText == null) return null;
        try {
            // 去除前缀
            String actualCipher = cipherText.startsWith(CIPHER_PREFIX)
                    ? cipherText.substring(CIPHER_PREFIX.length())
                    : cipherText;

            byte[] decoded = Base64.getDecoder().decode(actualCipher);
            if (decoded.length <= GCM_IV_LENGTH) {
                throw new EncryptionException("decrypt", PROVIDER_NAME, "密文格式无效");
            }

            byte[] iv = Arrays.copyOfRange(decoded, 0, GCM_IV_LENGTH);
            byte[] cipherBytes = Arrays.copyOfRange(decoded, GCM_IV_LENGTH, decoded.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] plainBytes = cipher.doFinal(cipherBytes);

            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (EncryptionException e) {
            throw e;
        } catch (Exception e) {
            throw new EncryptionException("decrypt", PROVIDER_NAME,
                    "解密失败 (密钥不匹配或密文损坏): " + e.getMessage(), e);
        }
    }

    @Override
    public boolean canDecrypt(String cipherText) {
        if (cipherText == null) return false;
        // 有前缀标识，或长度足够且是合法 base64
        if (ENCRYPTED_PATTERN.matcher(cipherText).matches()) return true;
        // 兼容无前缀的旧密文
        try {
            if (cipherText.length() < 24) return false;
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            return decoded.length >= (GCM_IV_LENGTH + GCM_TAG_LENGTH / 8 + 1);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    private SecretKey deriveKey(String input) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha256.digest(input.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (Exception e) {
            throw new IllegalStateException("密钥派生失败", e);
        }
    }
}
