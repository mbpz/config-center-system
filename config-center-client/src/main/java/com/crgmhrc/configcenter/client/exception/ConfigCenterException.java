/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.client.exception;

/**
 * Config Center SDK 异常
 */
public class ConfigCenterException extends RuntimeException {

    private final int statusCode;

    public ConfigCenterException(String message) {
        super(message);
        this.statusCode = -1;
    }

    public ConfigCenterException(String message, int statusCode) {
        super(message + " (HTTP " + statusCode + ")");
        this.statusCode = statusCode;
    }

    public ConfigCenterException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isAuthenticationError() {
        return statusCode == 401;
    }

    public boolean isAuthorizationError() {
        return statusCode == 403;
    }

    public boolean isNotFound() {
        return statusCode == 404;
    }
}
