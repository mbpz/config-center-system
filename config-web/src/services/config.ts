// SPDX-License-Identifier: Apache-2.0n// Copyright 2026 mbpz

import { request } from '@umijs/max';

export interface ConfigItem {
  id?: number;
  configKey: string;
  configValue: string;
  description: string;
  environment: string;
  version: string;
  status: string;
  createTime?: string;
  updateTime?: string;
}

export interface ConfigListParams {
  environment?: string;
  configKey?: string;
}

export interface HealthStatus {
  status: string;
  redis_available: boolean;
  cache_strategy: string;
  timestamp: number;
}

export interface CacheRefreshResult {
  success: boolean;
  message: string;
  environment?: string;
  timestamp: number;
}

export interface CacheInfo {
  key: string;
  environment: string;
  configKey: string;
  configValue: string;
  description: string;
  version: string;
  status: string;
  createTime?: string;
  updateTime?: string;
  ttl?: number;
  size?: number;
}

export interface CacheListResult {
  success: boolean;
  data: CacheInfo[];
  total: number;
  message: string;
  environment?: string;
  timestamp: number;
}

export interface CacheStats {
  success: boolean;
  total_cache_count: number;
  total_cache_size: number;
  average_ttl: number;
  environment_stats: Record<string, number>;
  redis_available: boolean;
  message: string;
  timestamp: number;
}

// 获取配置列表
export async function getConfigList(params: ConfigListParams) {
  const { environment = 'dev', configKey } = params;
  let url = `/api/v1/configs?environment=${environment}`;
  if (configKey) {
    url += `&configKey=${configKey}`;
  }
  return request<ConfigItem[]>(url, {
    method: 'GET',
  });
}

// 获取单个配置
export async function getConfig(
  configKey: string,
  environment: string = 'dev',
) {
  return request<ConfigItem>(
    `/api/v1/configs/${configKey}?environment=${environment}`,
    {
      method: 'GET',
    },
  );
}

// 创建配置
export async function createConfig(data: ConfigItem) {
  return request('/api/v1/configs', {
    method: 'POST',
    data,
  });
}

// 更新配置
export async function updateConfig(configKey: string, data: ConfigItem) {
  return request(`/api/v1/configs/${configKey}`, {
    method: 'PUT',
    data,
  });
}

// 删除配置
export async function deleteConfig(
  configKey: string,
  environment: string = 'dev',
) {
  return request(`/api/v1/configs/${configKey}?environment=${environment}`, {
    method: 'DELETE',
  });
}

// 检查健康状态
export async function checkHealth() {
  return request<HealthStatus>('/api/v1/health', {
    method: 'GET',
  });
}

// 检查Redis健康状态
export async function checkRedisHealth() {
  return request('/api/v1/health/redis', {
    method: 'GET',
  });
}

// 获取所有缓存信息列表
export async function getAllCacheInfo() {
  return request<CacheListResult>('/api/v1/health/cache/list', {
    method: 'GET',
  });
}

// 获取指定环境的缓存信息列表
export async function getEnvironmentCacheInfo(environment: string) {
  return request<CacheListResult>(`/api/v1/health/cache/list/${environment}`, {
    method: 'GET',
  });
}

// 获取缓存统计信息
export async function getCacheStats() {
  return request<CacheStats>('/api/v1/health/cache/stats', {
    method: 'GET',
  });
}

// 刷新指定环境缓存
export async function refreshEnvironmentCache(environment: string) {
  return request<CacheRefreshResult>(
    `/api/v1/health/cache/refresh/${environment}`,
    {
      method: 'POST',
    },
  );
}

// 刷新所有环境缓存
export async function refreshAllCache() {
  return request<CacheRefreshResult>('/api/v1/health/cache/refresh', {
    method: 'POST',
  });
}
