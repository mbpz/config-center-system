/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */
package com.crgmhrc.configcenter.config;

/**
 * 加密操作异常
 * 所有加解密失败应抛出此异常以便统一处理和记录
 */
public class EncryptionException extends RuntimeException {

    private final String operation; // "encrypt" or "decrypt"
    private final String providerName;

    public EncryptionException(String operation, String providerName, String message) {
        super(String.format("[%s] %s failed: %s", providerName, operation, message));
        this.operation = operation;
        this.providerName = providerName;
    }

    public EncryptionException(String operation, String providerName, String message, Throwable cause) {
        super(String.format("[%s] %s failed: %s", providerName, operation, message), cause);
        this.operation = operation;
        this.providerName = providerName;
    }

    public String getOperation() { return operation; }
    public String getProviderName() { return providerName; }
}
