// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 mbpz

export default (initialState: API.UserInfo | undefined) => {
  const roles = initialState?.roles || [];

  return {
    // 已认证用户可见
    canAccess: !!initialState?.authenticated,
    // ADMIN 角色可管理配置
    canAdmin: roles.includes('ROLE_ADMIN'),
    // USER 或 ADMIN 可查看
    canView: roles.includes('ROLE_USER') || roles.includes('ROLE_ADMIN'),
  };
};
