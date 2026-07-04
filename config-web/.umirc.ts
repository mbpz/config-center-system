// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 mbpz

import { defineConfig } from '@umijs/max';

export default defineConfig({
  antd: {},
  access: {},
  model: {},
  initialState: {},
  request: {},
  layout: {
    title: 'Config Center',
  },
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
  routes: [
    {
      path: '/login',
      component: './Login',
      layout: false,
    },
    {
      path: '/',
      redirect: '/config',
    },
    {
      name: '配置管理',
      path: '/config',
      component: './Config',
      access: 'canAdmin',
    },
    {
      name: '缓存管理',
      path: '/cache',
      component: './Cache',
      access: 'canView',
    },
  ],
  npmClient: 'yarn',
  // 错误边界
  dva: {},
});
