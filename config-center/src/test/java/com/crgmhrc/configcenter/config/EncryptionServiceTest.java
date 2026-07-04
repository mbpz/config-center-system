/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */
package com.crgmhrc.configcenter.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EncryptionService V2 单元测试
 */
class EncryptionServiceTest {

    private EncryptionService encryptionService;
    private static final String ENV = "test";

    @BeforeEach
    void setUp() {
        // 直接测试 LocalKmsProvider (V2 的默认实现)
        LocalKmsProvider provider = new LocalKmsProvider();
        ReflectionTestUtils.setField(provider, "masterKeyConfig", "test_master_key_for_unit_tests");
        provider.init();

        encryptionService = new EncryptionService();
        ReflectionTestUtils.setField(encryptionService, "localKmsProvider", provider);
        // 跳过注入 providers (测试环境无 Spring 容器)
        ReflectionTestUtils.setField(encryptionService, "injectedProviders", null);
        ReflectionTestUtils.setField(encryptionService, "encryptPatternsConfig", "*.password,*.secret");
        encryptionService.init();
    }

    @Test
    void encryptDecryptRoundtrip() {
        String plainText = "my-secret-password-123";
        String encrypted = encryptionService.encrypt(plainText, ENV);

        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);

        String decrypted = encryptionService.decrypt(encrypted, ENV);
        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptNull() {
        assertNull(encryptionService.encrypt(null, ENV));
    }

    @Test
    void decryptNull() {
        assertNull(encryptionService.decrypt(null, ENV));
    }

    @Test
    void encryptEmptyString() {
        String encrypted = encryptionService.encrypt("", ENV);
        assertNotNull(encrypted);
        String decrypted = encryptionService.decrypt(encrypted, ENV);
        assertEquals("", decrypted);
    }

    @Test
    void encryptUnicode() {
        String plainText = "中文密码🔐日本語テスト";
        String encrypted = encryptionService.encrypt(plainText, ENV);
        String decrypted = encryptionService.decrypt(encrypted, ENV);
        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptLongText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) sb.append('a');
        String plainText = sb.toString();
        String encrypted = encryptionService.encrypt(plainText, ENV);
        String decrypted = encryptionService.decrypt(encrypted, ENV);
        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptedValueDiffersFromPlaintext() {
        String plainText = "secret";
        String encrypted = encryptionService.encrypt(plainText, ENV);
        assertNotEquals(plainText, encrypted);
        assertFalse(encrypted.contains(plainText));
    }

    @Test
    void samePlaintextProducesDifferentCiphertext() {
        // GCM 模式每次使用随机 IV，相同明文应产生不同密文
        String plainText = "same-text";
        String encrypted1 = encryptionService.encrypt(plainText, ENV);
        String encrypted2 = encryptionService.encrypt(plainText, ENV);
        assertNotEquals(encrypted1, encrypted2);

        // 但解密后应相同
        assertEquals(plainText, encryptionService.decrypt(encrypted1, ENV));
        assertEquals(plainText, encryptionService.decrypt(encrypted2, ENV));
    }

    @Test
    void isEncrypted_DetectsEncryptedValue() {
        String plainText = "hello-world";
        String encrypted = encryptionService.encrypt(plainText, ENV);

        assertTrue(encryptionService.isEncrypted(encrypted));
        assertFalse(encryptionService.isEncrypted(plainText));
        assertFalse(encryptionService.isEncrypted("short"));
        assertFalse(encryptionService.isEncrypted(null));
    }

    @Test
    void decryptInvalidBase64() {
        assertThrows(EncryptionException.class, () -> {
            encryptionService.decrypt("!!!invalid-base64!!!", ENV);
        });
    }

    @Test
    void shouldAutoEncrypt_MatchesPattern() {
        assertTrue(encryptionService.shouldAutoEncrypt("db.password"));
        assertTrue(encryptionService.shouldAutoEncrypt("api.secret"));
        assertFalse(encryptionService.shouldAutoEncrypt("app.name"));
        assertFalse(encryptionService.shouldAutoEncrypt(null));
    }

    @Test
    void isEncryptionEnabled() {
        assertTrue(encryptionService.isEncryptionEnabled());
        assertEquals("local", encryptionService.getPrimaryProviderName());
    }
}
