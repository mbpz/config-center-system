// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 mbpz

import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
import zhCN from '../locales/zh-CN';
import enUS from '../locales/en-US';

type LocaleType = 'zh-CN' | 'en-US';

const messages: Record<LocaleType, Record<string, string>> = {
  'zh-CN': zhCN,
  'en-US': enUS,
};

interface LocaleContextType {
  locale: LocaleType;
  setLocale: (locale: LocaleType) => void;
  t: (key: string, params?: Record<string, any>) => string;
}

const LocaleContext = createContext<LocaleContextType>({
  locale: 'zh-CN',
  setLocale: () => {},
  t: (key: string) => key,
});

export function LocaleProvider({ children }: { children: React.ReactNode }) {
  const [locale, setLocaleState] = useState<LocaleType>(() => {
    return (localStorage.getItem('locale') as LocaleType) || 'zh-CN';
  });

  const setLocale = useCallback((newLocale: LocaleType) => {
    setLocaleState(newLocale);
    localStorage.setItem('locale', newLocale);
  }, []);

  const t = useCallback((key: string, params?: Record<string, any>) => {
    let text = messages[locale][key] || key;
    if (params) {
      Object.entries(params).forEach(([k, v]) => {
        text = text.replace(`{${k}}`, String(v));
      });
    }
    return text;
  }, [locale]);

  return (
    <LocaleContext.Provider value={{ locale, setLocale, t }}>
      {children}
    </LocaleContext.Provider>
  );
}

export function useLocale() {
  return useContext(LocaleContext);
}
