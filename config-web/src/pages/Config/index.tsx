// SPDX-License-Identifier: Apache-2.0n// Copyright 2026 mbpz

import {
  checkHealth,
  ConfigItem,
  ConfigListParams,
  createConfig,
  deleteConfig,
  getConfigList,
  HealthStatus,
  refreshAllCache,
  refreshEnvironmentCache,
  updateConfig,
} from '@/services/config';
import {
  CloudOutlined,
  DatabaseOutlined,
  DeleteOutlined,
  EditOutlined,
  PlusOutlined,
  ReloadOutlined,
  SearchOutlined,
  SettingOutlined,
  SyncOutlined,
} from '@ant-design/icons';
import {
  Badge,
  Button,
  Card,
  Col,
  Dropdown,
  Form,
  Input,
  Menu,
  message,
  Modal,
  Popconfirm,
  Row,
  Select,
  Space,
  Statistic,
  Table,
  Tag,
  Tooltip,
} from 'antd';
import { history, useModel } from '@umijs/max';
import React, { useEffect, useState } from 'react';

const { Option } = Select;
const { TextArea } = Input;

const ConfigList: React.FC = () => {
  const { initialState } = useModel('@@initialState');
  const [configs, setConfigs] = useState<ConfigItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingConfig, setEditingConfig] = useState<ConfigItem | null>(null);
  const [form] = Form.useForm();
  const [searchForm] = Form.useForm();
  const [selectedEnvironment, setSelectedEnvironment] = useState('dev');
  const [healthStatus, setHealthStatus] = useState<HealthStatus | null>(null);
  const [cacheRefreshing, setCacheRefreshing] = useState(false);

  // 获取配置列表
  const fetchConfigs = async (params: ConfigListParams = {}) => {
    setLoading(true);
    try {
      const response = await getConfigList({
        environment: selectedEnvironment,
        ...params,
      });
      setConfigs(response || []);
    } catch (error) {
      message.error('获取配置列表失败');
      console.error('获取配置列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 获取健康状态
  const fetchHealthStatus = async () => {
    try {
      const response = await checkHealth();
      setHealthStatus(response);
    } catch (error) {
      console.error('获取健康状态失败:', error);
    }
  };

  // 刷新指定环境缓存
  const handleRefreshEnvironmentCache = async (environment: string) => {
    setCacheRefreshing(true);
    try {
      const result = await refreshEnvironmentCache(environment);
      if (result.success) {
        message.success(result.message);
        // 重新获取配置列表
        fetchConfigs();
      } else {
        message.error(result.message);
      }
    } catch (error) {
      message.error('刷新缓存失败');
      console.error('刷新缓存失败:', error);
    } finally {
      setCacheRefreshing(false);
    }
  };

  // 刷新所有环境缓存
  const handleRefreshAllCache = async () => {
    setCacheRefreshing(true);
    try {
      const result = await refreshAllCache();
      if (result.success) {
        message.success(result.message);
        // 重新获取配置列表
        fetchConfigs();
      } else {
        message.error(result.message);
      }
    } catch (error) {
      message.error('刷新所有缓存失败');
      console.error('刷新所有缓存失败:', error);
    } finally {
      setCacheRefreshing(false);
    }
  };

  // 创建或更新配置
  const handleSubmit = async (values: any) => {
    try {
      if (editingConfig) {
        // 更新配置
        await updateConfig(values.configKey, {
          ...values,
          environment: selectedEnvironment,
        });
        message.success('配置更新成功');
      } else {
        // 创建配置
        await createConfig({
          ...values,
          environment: selectedEnvironment,
        });
        message.success('配置创建成功');
      }
      setModalVisible(false);
      form.resetFields();
      setEditingConfig(null);
      fetchConfigs();
    } catch (error) {
      message.error(editingConfig ? '配置更新失败' : '配置创建失败');
      console.error('提交配置失败:', error);
    }
  };

  // 删除配置
  const handleDelete = async (configKey: string) => {
    try {
      await deleteConfig(configKey, selectedEnvironment);
      message.success('配置删除成功');
      fetchConfigs();
    } catch (error) {
      message.error('配置删除失败');
      console.error('删除配置失败:', error);
    }
  };

  // 编辑配置
  const handleEdit = (record: ConfigItem) => {
    setEditingConfig(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  // 新增配置
  const handleAdd = () => {
    setEditingConfig(null);
    form.resetFields();
    setModalVisible(true);
  };

  // 搜索配置
  const handleSearch = (values: any) => {
    fetchConfigs(values);
  };

  // 环境切换
  const handleEnvironmentChange = (environment: string) => {
    setSelectedEnvironment(environment);
    fetchConfigs();
  };

  // 缓存管理菜单
  const getCacheMenu = () => (
    <Menu>
      <Menu.Item
        key="refresh-current"
        icon={<SyncOutlined spin={cacheRefreshing} />}
        onClick={() => handleRefreshEnvironmentCache(selectedEnvironment)}
        disabled={cacheRefreshing}
      >
        刷新当前环境缓存
      </Menu.Item>
      <Menu.Item
        key="refresh-all"
        icon={<SyncOutlined spin={cacheRefreshing} />}
        onClick={handleRefreshAllCache}
        disabled={cacheRefreshing}
      >
        刷新所有环境缓存
      </Menu.Item>
      <Menu.Divider />
      <Menu.Item key="status" disabled>
        <Space>
          <span>Redis状态:</span>
          <Badge
            status={healthStatus?.redis_available ? 'success' : 'error'}
            text={healthStatus?.redis_available ? '正常' : '异常'}
          />
        </Space>
      </Menu.Item>
      <Menu.Item key="strategy" disabled>
        <span>缓存策略: {healthStatus?.cache_strategy}</span>
      </Menu.Item>
    </Menu>
  );

  // 认证检查：未登录跳转 /login
  useEffect(() => {
    if (initialState && !initialState.authenticated) {
      history.replace('/login');
    }
  }, [initialState]);

  useEffect(() => {
    if (initialState?.authenticated) {
      fetchConfigs();
      fetchHealthStatus();
    }
  }, []);

  const columns = [
    {
      title: '配置键',
      dataIndex: 'configKey',
      key: 'configKey',
      width: 200,
      ellipsis: true,
      render: (text: string) => (
        <Tooltip title={text}>
          <span>{text}</span>
        </Tooltip>
      ),
    },
    {
      title: '配置值',
      dataIndex: 'configValue',
      key: 'configValue',
      width: 300,
      ellipsis: true,
      render: (text: string) => (
        <Tooltip title={text}>
          <span>{text}</span>
        </Tooltip>
      ),
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      width: 200,
      ellipsis: true,
      render: (text: string) => (
        <Tooltip title={text}>
          <span>{text}</span>
        </Tooltip>
      ),
    },
    {
      title: '环境',
      dataIndex: 'environment',
      key: 'environment',
      width: 100,
      render: (text: string) => {
        const color =
          text === 'prod' ? 'red' : text === 'test' ? 'orange' : 'green';
        return <Tag color={color}>{text.toUpperCase()}</Tag>;
      },
    },
    {
      title: '版本',
      dataIndex: 'version',
      key: 'version',
      width: 100,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (text: string) => (
        <Tag color={text === 'ACTIVE' ? 'green' : 'red'}>{text}</Tag>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 180,
      render: (text: string) => (text ? new Date(text).toLocaleString() : '-'),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right' as const,
      render: (_: any, record: ConfigItem) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个配置吗？"
            onConfirm={() => handleDelete(record.configKey)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      {/* 统计信息 */}
      {healthStatus && (
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Card>
              <Statistic
                title="服务状态"
                value={healthStatus.status}
                valueStyle={{
                  color: healthStatus.status === 'UP' ? '#3f8600' : '#cf1322',
                }}
                prefix={healthStatus.status === 'UP' ? '🟢' : '🔴'}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="Redis状态"
                value={healthStatus.redis_available ? '正常' : '异常'}
                valueStyle={{
                  color: healthStatus.redis_available ? '#3f8600' : '#cf1322',
                }}
                prefix={
                  healthStatus.redis_available ? (
                    <CloudOutlined />
                  ) : (
                    <DatabaseOutlined />
                  )
                }
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="缓存策略"
                value={healthStatus.cache_strategy}
                valueStyle={{ fontSize: '14px' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="配置总数"
                value={configs.length}
                prefix={<DatabaseOutlined />}
              />
            </Card>
          </Col>
        </Row>
      )}

      <Card
        title="配置管理"
        extra={
          <Space>
            <Select
              value={selectedEnvironment}
              onChange={handleEnvironmentChange}
              style={{ width: 120 }}
            >
              <Option value="dev">开发环境</Option>
              <Option value="test">测试环境</Option>
              <Option value="prod">生产环境</Option>
            </Select>

            {/* 缓存管理下拉菜单 */}
            <Dropdown overlay={getCacheMenu()} trigger={['click']}>
              <Button icon={<SettingOutlined />}>缓存管理</Button>
            </Dropdown>

            <Button
              icon={<ReloadOutlined />}
              onClick={() => {
                fetchConfigs();
                fetchHealthStatus();
              }}
            >
              刷新
            </Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              新增配置
            </Button>
          </Space>
        }
      >
        {/* 搜索表单 */}
        <Form
          form={searchForm}
          layout="inline"
          onFinish={handleSearch}
          style={{ marginBottom: 16 }}
        >
          <Form.Item name="configKey" label="配置键">
            <Input placeholder="请输入配置键" style={{ width: 200 }} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button
                type="primary"
                icon={<SearchOutlined />}
                htmlType="submit"
              >
                搜索
              </Button>
              <Button
                onClick={() => {
                  searchForm.resetFields();
                  fetchConfigs();
                }}
              >
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>

        {/* 配置表格 */}
        <Table
          columns={columns}
          dataSource={configs}
          rowKey="id"
          loading={loading}
          scroll={{ x: 1200 }}
          pagination={{
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
            defaultPageSize: 10,
            pageSizeOptions: ['10', '20', '50', '100'],
          }}
        />
      </Card>

      {/* 新增/编辑配置弹窗 */}
      <Modal
        title={editingConfig ? '编辑配置' : '新增配置'}
        open={modalVisible}
        onCancel={() => {
          setModalVisible(false);
          setEditingConfig(null);
          form.resetFields();
        }}
        footer={null}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{
            version: '1.0.0',
            status: 'ACTIVE',
          }}
        >
          <Form.Item
            name="configKey"
            label="配置键"
            rules={[{ required: true, message: '请输入配置键' }]}
          >
            <Input placeholder="请输入配置键" disabled={!!editingConfig} />
          </Form.Item>

          <Form.Item
            name="configValue"
            label="配置值"
            rules={[{ required: true, message: '请输入配置值' }]}
          >
            <TextArea rows={3} placeholder="请输入配置值" />
          </Form.Item>

          <Form.Item
            name="description"
            label="描述"
            rules={[{ required: true, message: '请输入描述' }]}
          >
            <TextArea rows={2} placeholder="请输入描述" />
          </Form.Item>

          <Form.Item
            name="version"
            label="版本"
            rules={[{ required: true, message: '请输入版本' }]}
          >
            <Input placeholder="请输入版本" />
          </Form.Item>

          <Form.Item
            name="status"
            label="状态"
            rules={[{ required: true, message: '请选择状态' }]}
          >
            <Select placeholder="请选择状态">
              <Option value="ACTIVE">启用</Option>
              <Option value="INACTIVE">禁用</Option>
            </Select>
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                {editingConfig ? '更新' : '创建'}
              </Button>
              <Button
                onClick={() => {
                  setModalVisible(false);
                  setEditingConfig(null);
                  form.resetFields();
                }}
              >
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default ConfigList;
