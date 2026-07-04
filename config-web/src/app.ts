// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 mbpz

import React from 'react';
import { LocaleProvider } from '@/context/LocaleContext';
import ErrorBoundary from '@/components/ErrorBoundary';

// 全局初始化数据配置
export async function getInitialState(): Promise<API.UserInfo> {
  try {
    const res = await fetch('/api/v1/auth/me', { credentials: 'include' });
    if (res.ok) {
      const data = await res.json();
      return {
        name: data.username || 'unknown',
        authenticated: data.authenticated || false,
        roles: data.roles || [],
      };
    }
  } catch (e) {
    // 后端未启动或未认证时返回匿名状态
  }
  return {
    name: 'Not logged in',
    authenticated: false,
    roles: ['ROLE_GUEST'],
  };
}

export const layout = () => {
  return {
    title: 'Config Center',
    logo: 'https://img.alicdn.com/tfs/TB1YHEpwUT1gK0jSZFhXXaAtVXa-28-27.svg',
    menu: {
      locale: false,
    },
  };
};

// 用 ErrorBoundary + LocaleProvider 包裹应用
export const rootContainer = (container: any) => {
  return React.createElement(
    ErrorBoundary,
    null,
    React.createElement(LocaleProvider, null, container)
  );
};
