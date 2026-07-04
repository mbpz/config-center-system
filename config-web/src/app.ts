// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 mbpz

// 运行时配置

// 全局初始化数据配置，用于 Layout 用户信息和权限初始化
// 更多信息见文档：https://umijs.org/docs/api/runtime-config#getinitialstate
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
    name: '未登录',
    authenticated: false,
    roles: ['ROLE_GUEST'],
  };
}

export const layout = () => {
  return {
    logo: 'https://img.alicdn.com/tfs/TB1YHEpwUT1gK0jSZFhXXaAtVXa-28-27.svg',
    menu: {
      locale: false,
    },
  };
};

// 禁用 React 严格模式以减少 findDOMNode 警告
export const rootContainer = (container: any) => {
  return container;
};
