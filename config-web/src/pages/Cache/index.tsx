// SPDX-License-Identifier: Apache-2.0n// Copyright 2026 mbpz

import {
  CacheInfo,
  CacheStats,
  checkHealth,
  checkRedisHealth,
  getAllCacheInfo,
  getCacheStats,
  HealthStatus,
  refreshAllCache,
  refreshEnvironmentCache,
} from '@/services/config';
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  CloudOutlined,
  DatabaseOutlined,
  InfoCircleOutlined,
  ReloadOutlined,
  SettingOutlined,
  SyncOutlined,
} from '@ant-design/icons';
import {
  Alert,
  Badge,
  Button,
  Card,
  Col,
  Descriptions,
  Divider,
  message,
  Popconfirm,
  Row,
  Space,
  Statistic,
  Table,
  Tag,
  Tooltip,
} from 'antd';
import React, { useEffect, useState } from 'react';

const CacheManagement: React.FC = () => {
  const [healthStatus, setHealthStatus] = useState<HealthStatus | null>(null);
  const [redisHealth, setRedisHealth] = useState<any>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [lastRefreshTime, setLastRefreshTime] = useState<Date | null>(null);
  const [cacheData, setCacheData] = useState<CacheInfo[]>([]);
  const [cacheStats, setCacheStats] = useState<CacheStats | null>(null);
  const [loading, setLoading] = useState(false);

  // 获取健康状态
  const fetchHealthStatus = async () => {
    try {
      const response = await checkHealth();
      setHealthStatus(response);
    } catch (error) {
      console.error('获取健康状态失败:', error);
    }
  };

  // 获取Redis健康状态
  const fetchRedisHealth = async () => {
    try {
      const response = await checkRedisHealth();
      setRedisHealth(response);
    } catch (error) {
      console.error('获取Redis健康状态失败:', error);
    }
  };

  // 获取缓存列表
  const fetchCacheList = async () => {
    setLoading(true);
    try {
      const response = await getAllCacheInfo();
      if (response.success) {
        setCacheData(response.data || []);
      } else {
        message.error(response.message);
      }
    } catch (error) {
      message.error('获取缓存列表失败');
      console.error('获取缓存列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 获取缓存统计信息
  const fetchCacheStats = async () => {
    try {
      const response = await getCacheStats();
      if (response.success) {
        setCacheStats(response);
      } else {
        console.error('获取缓存统计失败:', response.message);
      }
    } catch (error) {
      console.error('获取缓存统计失败:', error);
    }
  };

  // 刷新指定环境缓存
  const handleRefreshEnvironmentCache = async (environment: string) => {
    setRefreshing(true);
    try {
      const result = await refreshEnvironmentCache(environment);
      if (result.success) {
        message.success(result.message);
        setLastRefreshTime(new Date());
        // 重新获取数据
        fetchHealthStatus();
        fetchRedisHealth();
        fetchCacheList();
        fetchCacheStats();
      } else {
        message.error(result.message);
      }
    } catch (error) {
      message.error('刷新缓存失败');
      console.error('刷新缓存失败:', error);
    } finally {
      setRefreshing(false);
    }
  };

  // 刷新所有环境缓存
  const handleRefreshAllCache = async () => {
    setRefreshing(true);
    try {
      const result = await refreshAllCache();
      if (result.success) {
        message.success(result.message);
        setLastRefreshTime(new Date());
        // 重新获取数据
        fetchHealthStatus();
        fetchRedisHealth();
        fetchCacheList();
        fetchCacheStats();
      } else {
        message.error(result.message);
      }
    } catch (error) {
      message.error('刷新所有缓存失败');
      console.error('刷新所有缓存失败:', error);
    } finally {
      setRefreshing(false);
    }
  };

  // 格式化TTL显示
  const formatTtl = (ttl: number | undefined): string => {
    if (!ttl || ttl <= 0) return '已过期';
    if (ttl < 60) return `${ttl}秒`;
    if (ttl < 3600) return `${Math.floor(ttl / 60)}分钟`;
    return `${Math.floor(ttl / 3600)}小时${Math.floor((ttl % 3600) / 60)}分钟`;
  };

  // 格式化大小显示
  const formatSize = (size: number | undefined): string => {
    if (!size) return '0B';
    if (size < 1024) return `${size}B`;
    if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`;
    return `${(size / (1024 * 1024)).toFixed(1)}MB`;
  };

  useEffect(() => {
    fetchHealthStatus();
    fetchRedisHealth();
    fetchCacheList();
    fetchCacheStats();
  }, []);

  const columns = [
    {
      title: '缓存键',
      dataIndex: 'key',
      key: 'key',
      width: 300,
      ellipsis: true,
      render: (text: string) => (
        <Tooltip title={text}>
          <span style={{ fontFamily: 'monospace' }}>{text}</span>
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
      title: '配置键',
      dataIndex: 'configKey',
      key: 'configKey',
      width: 150,
    },
    {
      title: '配置值',
      dataIndex: 'configValue',
      key: 'configValue',
      width: 200,
      ellipsis: true,
      render: (text: string) => (
        <Tooltip title={text}>
          <span>{text}</span>
        </Tooltip>
      ),
    },
    {
      title: '大小',
      dataIndex: 'size',
      key: 'size',
      width: 100,
      render: (size: number) => <Tag color="blue">{formatSize(size)}</Tag>,
    },
    {
      title: '剩余时间',
      dataIndex: 'ttl',
      key: 'ttl',
      width: 120,
      render: (ttl: number) => <Tag color="green">{formatTtl(ttl)}</Tag>,
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
  ];

  return (
    <div style={{ padding: '24px' }}>
      {/* 页面标题 */}
      <Card style={{ marginBottom: 16 }}>
        <Space align="center">
          <SettingOutlined style={{ fontSize: '24px', color: '#1890ff' }} />
          <h2 style={{ margin: 0 }}>缓存管理</h2>
        </Space>
      </Card>

      {/* 系统状态概览 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="系统状态"
              value={healthStatus?.status || 'UNKNOWN'}
              valueStyle={{
                color: healthStatus?.status === 'UP' ? '#3f8600' : '#cf1322',
                fontSize: '16px',
              }}
              prefix={
                healthStatus?.status === 'UP' ? (
                  <CheckCircleOutlined />
                ) : (
                  <CloseCircleOutlined />
                )
              }
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Redis状态"
              value={healthStatus?.redis_available ? '正常' : '异常'}
              valueStyle={{
                color: healthStatus?.redis_available ? '#3f8600' : '#cf1322',
                fontSize: '16px',
              }}
              prefix={
                healthStatus?.redis_available ? (
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
              title="缓存总数"
              value={cacheStats?.total_cache_count || 0}
              valueStyle={{ fontSize: '16px' }}
              prefix={<DatabaseOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="最后刷新"
              value={
                lastRefreshTime
                  ? lastRefreshTime.toLocaleTimeString()
                  : '未刷新'
              }
              valueStyle={{ fontSize: '14px' }}
              prefix={<ReloadOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* 缓存统计信息 */}
      {cacheStats && (
        <Card title="缓存统计信息" style={{ marginBottom: 16 }}>
          <Row gutter={16}>
            <Col span={6}>
              <Statistic
                title="总缓存大小"
                value={formatSize(cacheStats.total_cache_size)}
                valueStyle={{ fontSize: '14px' }}
              />
            </Col>
            <Col span={6}>
              <Statistic
                title="平均TTL"
                value={formatTtl(cacheStats.average_ttl)}
                valueStyle={{ fontSize: '14px' }}
              />
            </Col>
            <Col span={12}>
              <div>
                <strong>环境分布:</strong>
                <Space style={{ marginLeft: 8 }}>
                  {Object.entries(cacheStats.environment_stats).map(
                    ([env, count]) => (
                      <Tag
                        key={env}
                        color={
                          env === 'prod'
                            ? 'red'
                            : env === 'test'
                            ? 'orange'
                            : 'green'
                        }
                      >
                        {env.toUpperCase()}: {count}
                      </Tag>
                    ),
                  )}
                </Space>
              </div>
            </Col>
          </Row>
        </Card>
      )}

      {/* Redis详细信息 */}
      {redisHealth && (
        <Card title="Redis详细信息" style={{ marginBottom: 16 }}>
          <Descriptions bordered size="small" column={2}>
            <Descriptions.Item label="连接状态">
              <Badge
                status={redisHealth.redis_available ? 'success' : 'error'}
                text={redisHealth.redis_available ? '正常' : '异常'}
              />
            </Descriptions.Item>
            <Descriptions.Item label="消息">
              {redisHealth.message}
            </Descriptions.Item>
            <Descriptions.Item label="时间戳">
              {new Date(redisHealth.timestamp).toLocaleString()}
            </Descriptions.Item>
          </Descriptions>
        </Card>
      )}

      {/* 缓存操作 */}
      <Card title="缓存操作" style={{ marginBottom: 16 }}>
        <Space size="middle">
          <Button
            type="primary"
            icon={<SyncOutlined spin={refreshing} />}
            onClick={() => handleRefreshAllCache()}
            loading={refreshing}
          >
            刷新所有环境缓存
          </Button>

          <Popconfirm
            title="确定要刷新开发环境缓存吗？"
            onConfirm={() => handleRefreshEnvironmentCache('dev')}
            okText="确定"
            cancelText="取消"
          >
            <Button
              icon={<SyncOutlined spin={refreshing} />}
              loading={refreshing}
            >
              刷新开发环境
            </Button>
          </Popconfirm>

          <Popconfirm
            title="确定要刷新测试环境缓存吗？"
            onConfirm={() => handleRefreshEnvironmentCache('test')}
            okText="确定"
            cancelText="取消"
          >
            <Button
              icon={<SyncOutlined spin={refreshing} />}
              loading={refreshing}
            >
              刷新测试环境
            </Button>
          </Popconfirm>

          <Popconfirm
            title="确定要刷新生产环境缓存吗？"
            onConfirm={() => handleRefreshEnvironmentCache('prod')}
            okText="确定"
            cancelText="取消"
          >
            <Button
              icon={<SyncOutlined spin={refreshing} />}
              loading={refreshing}
            >
              刷新生产环境
            </Button>
          </Popconfirm>

          <Button
            icon={<ReloadOutlined />}
            onClick={() => {
              fetchHealthStatus();
              fetchRedisHealth();
              fetchCacheList();
              fetchCacheStats();
            }}
          >
            刷新状态
          </Button>
        </Space>
      </Card>

      {/* 缓存信息说明 */}
      <Alert
        message="缓存说明"
        description={
          <div>
            <p>
              • <strong>三级缓存策略</strong>：本地缓存 → Redis 缓存 → 数据库
            </p>
            <p>
              • <strong>缓存过期时间</strong>：Redis 缓存 1 小时，本地缓存 5
              分钟
            </p>
            <p>
              • <strong>缓存键格式</strong>：config:配置键:环境
            </p>
            <p>
              • <strong>自动降级</strong>：Redis 不可用时自动降级到数据库
            </p>
          </div>
        }
        type="info"
        showIcon
        icon={<InfoCircleOutlined />}
        style={{ marginBottom: 16 }}
      />

      {/* 缓存列表 */}
      <Card
        title="缓存列表"
        extra={
          <Space>
            <span>共 {cacheData.length} 条缓存记录</span>
          </Space>
        }
      >
        <Table
          columns={columns}
          dataSource={cacheData}
          rowKey="key"
          loading={loading}
          pagination={{
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
            defaultPageSize: 10,
            pageSizeOptions: ['10', '20', '50', '100'],
          }}
          size="small"
        />
      </Card>

      <Divider />

      {/* 操作日志 */}
      <Card title="操作日志" size="small">
        <div style={{ color: '#666', fontSize: '12px' }}>
          <p>• {new Date().toLocaleString()} - 页面加载完成</p>
          {lastRefreshTime && (
            <p>• {lastRefreshTime.toLocaleString()} - 缓存刷新完成</p>
          )}
        </div>
      </Card>
    </div>
  );
};

export default CacheManagement;
