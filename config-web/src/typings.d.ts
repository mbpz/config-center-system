// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 mbpz

declare namespace API {
  interface UserInfo {
    name?: string;
    authenticated: boolean;
    roles: string[];
  }

  interface AuthMeResponse {
    authenticated: boolean;
    username: string;
    roles: string[];
  }
}
