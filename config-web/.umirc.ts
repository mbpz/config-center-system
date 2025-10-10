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
      path: '/',
      redirect: '/config',
    },
    {
      name: '配置管理',
      path: '/config',
      component: './Config',
    },
    {
      name: '缓存管理',
      path: '/cache',
      component: './Cache',
    },
    {
      name: '首页',
      path: '/home',
      component: './Home',
    },
    {
      name: '权限演示',
      path: '/access',
      component: './Access',
    },
    {
      name: ' CRUD 示例',
      path: '/table',
      component: './Table',
    },
  ],
  npmClient: 'yarn',
});
