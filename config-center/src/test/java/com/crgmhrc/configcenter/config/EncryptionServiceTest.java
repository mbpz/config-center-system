/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EncryptionService 单元测试
 */
class EncryptionServiceTest {

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService();
        // 设置固定密钥用于测试
        ReflectionTestUtils.setField(encryptionService, "masterKeyConfig", "test_master_key_for_unit_tests");
        encryptionService.init();
    }

    @Test
    void encryptDecryptRoundtrip() {
        String plainText = "my-secret-password-123";
        String encrypted = encryptionService.encrypt(plainText);

        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);

        String decrypted = encryptionService.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptNull() {
        assertNull(encryptionService.encrypt(null));
    }

    @Test
    void decryptNull() {
        assertNull(encryptionService.decrypt(null));
    }

    @Test
    void encryptEmptyString() {
        String encrypted = encryptionService.encrypt("");
        assertNotNull(encrypted);
        String decrypted = encryptionService.decrypt(encrypted);
        assertEquals("", decrypted);
    }

    @Test
    void encryptUnicode() {
        String plainText = "中文密码🔐日本語テスト";
        String encrypted = encryptionService.encrypt(plainText);
        String decrypted = encryptionService.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptLongText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) sb.append('a');
        String plainText = sb.toString();
        String encrypted = encryptionService.encrypt(plainText);
        String decrypted = encryptionService.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptedValueDiffersFromPlaintext() {
        String plainText = "secret";
        String encrypted = encryptionService.encrypt(plainText);
        assertNotEquals(plainText, encrypted);
        assertFalse(encrypted.contains(plainText));
    }

    @Test
    void samePlaintextProducesDifferentCiphertext() {
        // GCM 模式每次使用随机 IV，相同明文应产生不同密文
        String plainText = "same-text";
        String encrypted1 = encryptionService.encrypt(plainText);
        String encrypted2 = encryptionService.encrypt(plainText);
        assertNotEquals(encrypted1, encrypted2);

        // 但解密后应相同
        assertEquals(plainText, encryptionService.decrypt(encrypted1));
        assertEquals(plainText, encryptionService.decrypt(encrypted2));
    }

    @Test
    void isEncrypted_DetectsEncryptedValue() {
        String plainText = "hello-world";
        String encrypted = encryptionService.encrypt(plainText);

        assertTrue(encryptionService.isEncrypted(encrypted));
        assertFalse(encryptionService.isEncrypted(plainText));
        assertFalse(encryptionService.isEncrypted("short"));
        assertFalse(encryptionService.isEncrypted(null));
    }

    @Test
    void decryptInvalidBase64() {
        assertThrows(IllegalStateException.class, () -> {
            encryptionService.decrypt("!!!invalid-base64!!!");
        });
    }

    @Test
    void decryptTooShortValue() {
        // 解码后长度不足 28 字节 (12 IV + 16 tag)
        String shortBase64 = "AQID"; // 解码后仅 3 字节
        assertThrows(IllegalStateException.class, () -> {
            encryptionService.decrypt(shortBase64);
        });
    }

    @Test
    void isEncryptionEnabled() {
        assertTrue(encryptionService.isEncryptionEnabled());
    }
}
