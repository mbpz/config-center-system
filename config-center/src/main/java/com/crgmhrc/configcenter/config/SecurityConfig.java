/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 mbpz
 */

package com.crgmhrc.configcenter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${CC_ADMIN_PASSWORD:admin}")
    private String adminPassword;

    @Value("${CC_USER_PASSWORD:}")
    private String userPassword;

    @Value("${CC_CORS_ALLOWED_ORIGINS:*}")
    private String corsAllowedOrigins;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 内存用户存储（Phase 1 简单实现，Phase 2 可扩展为 DB/LDAP/OAuth）
        auth.inMemoryAuthentication()
                .passwordEncoder(passwordEncoder())
                .withUser("admin")
                .password(passwordEncoder().encode(adminPassword))
                .roles("ADMIN", "USER")
                .and()
                .withUser("user")
                .password(passwordEncoder().encode(userPassword))
                .roles("USER");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 启用 CORS 并禁用 CSRF（API-only, 后续可启用 JWT/Token 机制）
            .cors().and()
            .csrf().disable()
            // 无状态 session
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .and()
            .authorizeRequests()
                // 健康检查端点允许匿名
                .antMatchers("/api/v1/health/**").permitAll()
                // 认证相关端点允许匿名（登录接口本身不认证）
                .antMatchers("/api/v1/auth/**").permitAll()
                // 配置写操作（创建/更新/删除）→ 需要 ADMIN 角色
                .antMatchers("/api/v1/configs/**").hasRole("ADMIN")
                // 任何其他请求需要认证
                .anyRequest().authenticated()
            .and()
            .httpBasic()
            .and()
            .formLogin()
                .loginPage("/api/v1/auth/login")
                .permitAll()
            .and()
            .logout()
                .logoutUrl("/api/v1/auth/logout")
                .permitAll();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.asList(corsAllowedOrigins.split(","));
        if (origins.contains("*")) {
            configuration.addAllowedOriginPattern("*");
        } else {
            configuration.setAllowedOrigins(origins);
        }
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
