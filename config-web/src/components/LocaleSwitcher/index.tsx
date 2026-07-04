// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 mbpz

import { GlobalOutlined } from '@ant-design/icons';
import { Select } from 'antd';
import { useLocale } from '@/context/LocaleContext';
import { useEffect } from 'react';

export default function LocaleSwitcher() {
  const { locale, setLocale } = useLocale();

  // 同步 antd locale
  useEffect(() => {
    // 可以在此处切换 antd 组件的语言
  }, [locale]);

  return (
    <Select
      value={locale}
      onChange={(val) => setLocale(val)}
      style={{ width: 100 }}
      size="small"
      bordered={false}
      suffixIcon={<GlobalOutlined />}
      options={[
        { label: '中文', value: 'zh-CN' },
        { label: 'English', value: 'en-US' },
      ]}
    />
  );
}
