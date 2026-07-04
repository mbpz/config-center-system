// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 mbpz

import { history, useModel } from '@umijs/max';
import { Button, Card, Form, Input, message } from 'antd';
import { useState } from 'react';

const LoginPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const { refresh } = useModel('@@initialState');

  const onFinish = async (values: { username: string; password: string }) => {
    setLoading(true);
    try {
      const res = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        credentials: 'include',
        body: new URLSearchParams(values).toString(),
      });

      if (res.ok || res.redirected) {
        message.success('登录成功');
        await refresh();
        history.push('/config');
      } else {
        message.error('用户名或密码错误');
      }
    } catch (e) {
      message.error('登录失败，请检查网络');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '100vh',
      background: '#f0f2f5',
    }}>
      <Card title="配置中心登录" style={{ width: 360 }}>
        <Form
          name="login"
          onFinish={onFinish}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            label="用户名"
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input placeholder="admin / user" />
          </Form.Item>

          <Form.Item
            label="密码"
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0 }}>
            <Button type="primary" htmlType="submit" loading={loading} block>
              登录
            </Button>
          </Form.Item>
        </Form>

        <div style={{ marginTop: 16, fontSize: 12, color: '#888' }}>
          <p>开发环境默认账号：admin / dev_admin</p>
          <p>首次生产启动时查看日志中的 bootstrap token</p>
        </div>
      </Card>
    </div>
  );
};

export default LoginPage;
