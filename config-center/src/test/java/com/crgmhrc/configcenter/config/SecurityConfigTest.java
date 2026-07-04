/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.config;

import com.crgmhrc.configcenter.entity.ConfigChangeLog;
import com.crgmhrc.configcenter.mapper.AuditMapper;
import com.crgmhrc.configcenter.service.ConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Spring Security 授权规则集成测试
 * 验证路径级别的认证和授权（不需要真实数据库连接）
 */
@SpringBootTest(properties = {
        "spring.redis.enabled=false"
})
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.yml")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock 数据层和数据库连接
    @MockBean
    private ConfigService configService;

    @MockBean
    private AuditMapper auditMapper;

    @MockBean
    private javax.sql.DataSource dataSource;

    @Test
    void healthEndpoint_anonymousAccess_allowed() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk());
    }

    @Test
    void authMeEndpoint_anonymousAccess_allowed() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk());
    }

    @Test
    void configsEndpoint_anonymousAccess_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/configs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void configsEndpoint_adminAccess_allowed() throws Exception {
        when(configService.getConfigsByEnvironment("dev")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/configs")
                        .header("Authorization", basicAuth("admin", "test_admin_pass")))
                .andExpect(status().isOk());
    }

    @Test
    void configsEndpoint_userAccess_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/configs")
                        .header("Authorization", basicAuth("user", "test_user_pass")))
                .andExpect(status().isForbidden());
    }

    @Test
    void configsCreate_adminAccess_allowed() throws Exception {
        String json = "{\"configKey\":\"test\",\"configValue\":\"val\",\"description\":\"desc\","
                + "\"environment\":\"dev\",\"version\":\"1.0\",\"status\":\"ACTIVE\"}";

        mockMvc.perform(post("/api/v1/configs")
                        .header("Authorization", basicAuth("admin", "test_admin_pass"))
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void wrongPassword_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/configs")
                        .header("Authorization", basicAuth("admin", "wrong_password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void auditEndpoint_userAccess_allowed() throws Exception {
        mockMvc.perform(get("/api/v1/audit")
                        .header("Authorization", basicAuth("user", "test_user_pass")))
                .andExpect(status().isOk());
    }

    @Test
    void auditEndpoint_adminAccess_allowed() throws Exception {
        mockMvc.perform(get("/api/v1/audit")
                        .header("Authorization", basicAuth("admin", "test_admin_pass")))
                .andExpect(status().isOk());
    }

    /**
     * 生成 HTTP Basic Auth header
     */
    private String basicAuth(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}
