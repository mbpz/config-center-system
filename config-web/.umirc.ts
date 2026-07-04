import { defineConfig } from '@umijs/max';

export default defineConfig({
  antd: {},
  access: {},
  model: {},
  initialState: {},
  request: {},
  layout: {
    title: '配置中心',
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
});
