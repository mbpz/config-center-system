#!/usr/bin/env python3
# SPDX-License-Identifier: Apache-2.0
# Copyright 2026 mbpz

"""
cc-cli: Config Center 命令行工具

用法:
    cc-cli get <key> [--env dev]
    cc-cli list [--env dev]
    cc-cli set <key> <value> [--desc "描述"] [--encrypted]
    cc-cli update <key> <value> [--env dev]
    cc-cli delete <key> [--env dev]
    cc-cli import <file> [--env dev] [--overwrite]
    cc-cli export [--env dev] [--format json|yaml] [--output file]
    cc-cli watch [--env dev]
    cc-cli audit [--key <key>] [--env dev]
    cc-cli health

环境变量:
    CC_SERVER    Config Center 地址 (默认: http://localhost:8080)
    CC_USER      用户名 (默认: admin)
    CC_PASS      密码 (默认: admin)
    CC_ENV       环境 (默认: dev)
"""

import argparse
import base64
import json
import os
import sys
import urllib.request
import urllib.error

# ======================== 配置 ========================

SERVER = os.environ.get("CC_SERVER", "http://localhost:8080").rstrip("/")
USER = os.environ.get("CC_USER", "admin")
PASS = os.environ.get("CC_PASS", "admin")
ENV = os.environ.get("CC_ENV", "dev")

# ======================== HTTP 工具 ========================

def basic_auth():
    return base64.b64encode(f"{USER}:{PASS}".encode()).decode()

def api_request(method, path, body=None, params=None):
    url = f"{SERVER}/api/v1{path}"
    if params:
        query = "&".join(f"{k}={v}" for k, v in params.items() if v is not None)
        if query:
            url += "?" + query

    headers = {"Authorization": f"Basic {basic_auth()}"}
    data = None
    if body is not None:
        headers["Content-Type"] = "application/json"
        data = json.dumps(body).encode()

    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            return json.loads(resp.read().decode()) if resp.status != 204 else {}
    except urllib.error.HTTPError as e:
        error_body = e.read().decode() if e.fp else ""
        print(f"❌ HTTP {e.code}: {error_body}", file=sys.stderr)
        sys.exit(1)
    except urllib.error.URLError as e:
        print(f"❌ 连接失败: {e.reason}", file=sys.stderr)
        sys.exit(1)

# ======================== 命令实现 ========================

def cmd_get(args):
    result = api_request("GET", f"/configs/{args.key}", params={"environment": args.env})
    print(f"Key: {result.get('configKey')}")
    print(f"Value: {result.get('configValue')}")
    print(f"Env: {result.get('environment')}")
    print(f"Status: {result.get('status')}")
    if result.get('encrypted'):
        print("🔒 已加密存储")

def cmd_list(args):
    result = api_request("GET", "/configs", params={"environment": args.env})
    items = result if isinstance(result, list) else result.get("data", [])
    if not items:
        print("暂无配置")
        return
    print(f"{'Key':<30} {'Value':<40} {'Status':<10} {'Env':<10}")
    print("-" * 90)
    for item in items:
        key = item.get("configKey", "")
        val = item.get("configValue", "")
        if item.get("encrypted"):
            val = "🔒 ****"
        elif len(val) > 35:
            val = val[:32] + "..."
        print(f"{key:<30} {val:<40} {item.get('status', ''):<10} {item.get('environment', ''):<10}")

def cmd_set(args):
    body = {
        "configKey": args.key,
        "configValue": args.value,
        "description": args.desc or "",
        "environment": args.env,
        "version": "1.0",
        "status": "ACTIVE",
        "encrypted": args.encrypted,
    }
    result = api_request("POST", "/configs", body=body)
    print(f"✅ 配置创建成功: {args.key}")

def cmd_update(args):
    body = {
        "configValue": args.value,
        "environment": args.env,
    }
    result = api_request("PUT", f"/configs/{args.key}", body=body, params={"environment": args.env})
    print(f"✅ 配置更新成功: {args.key}")

def cmd_delete(args):
    result = api_request("DELETE", f"/configs/{args.key}", params={"environment": args.env})
    print(f"✅ 配置删除成功: {args.key}")

def cmd_import(args):
    if not os.path.exists(args.file):
        print(f"❌ 文件不存在: {args.file}", file=sys.stderr)
        sys.exit(1)
    with open(args.file, "r") as f:
        content = f.read()

    # 使用 multipart/form-data
    import urllib.request
    boundary = "----CcCliBoundary7MA4YWxkTrZu0gW"
    filename = os.path.basename(args.file)

    body = (
        f"--{boundary}\r\n"
        f'Content-Disposition: form-data; name="file"; filename="{filename}"\r\n'
        f"Content-Type: application/octet-stream\r\n\r\n"
    ).encode() + content.encode() + (
        f"\r\n--{boundary}\r\n"
        f'Content-Disposition: form-data; name="environment"\r\n\r\n'
        f"{args.env}\r\n--{boundary}\r\n"
        f'Content-Disposition: form-data; name="overwrite"\r\n\r\n'
        f"{'true' if args.overwrite else 'false'}\r\n--{boundary}--\r\n"
    ).encode()

    url = f"{SERVER}/api/v1/configs/import"
    headers = {
        "Authorization": f"Basic {basic_auth()}",
        "Content-Type": f"multipart/form-data; boundary={boundary}",
    }
    req = urllib.request.Request(url, data=body, headers=headers, method="POST")
    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            result = json.loads(resp.read().decode())
            print(f"✅ {result.get('message', '导入成功')}")
    except urllib.error.HTTPError as e:
        print(f"❌ HTTP {e.code}: {e.read().decode()}", file=sys.stderr)
        sys.exit(1)

def cmd_export(args):
    fmt = args.format or "json"
    result = api_request("GET", "/configs/export", params={"env": args.env, "format": fmt})
    output = json.dumps(result, indent=2, ensure_ascii=False)

    if args.output:
        with open(args.output, "w") as f:
            f.write(output)
        print(f"✅ 已导出到: {args.output}")
    else:
        print(output)

def cmd_watch(args):
    """SSE 实时监听"""
    import urllib.request
    url = f"{SERVER}/api/v1/configs/stream?environment={args.env}"
    headers = {"Authorization": f"Basic {basic_auth()}"}
    req = urllib.request.Request(url, headers=headers)

    print(f"👂 监听配置变更 (环境: {args.env})... 按 Ctrl+C 退出")
    try:
        with urllib.request.urlopen(req, timeout=None) as resp:
            buffer = ""
            while True:
                chunk = resp.read(1024).decode()
                if not chunk:
                    break
                buffer += chunk
                while "\n\n" in buffer:
                    event_text, buffer = buffer.split("\n\n", 1)
                    for line in event_text.strip().split("\n"):
                        if line.startswith("event:"):
                            event_type = line[6:].strip()
                        elif line.startswith("data:"):
                            data = line[5:].strip()
                    if event_type == "config-change":
                        try:
                            event_data = json.loads(data)
                            print(f"📢 [{event_data.get('type')}] {event_data.get('configKey')} "
                                  f"(by {event_data.get('operator')})")
                        except json.JSONDecodeError:
                            pass
    except KeyboardInterrupt:
        print("\n👋 已停止监听")

def cmd_audit(args):
    result = api_request("GET", "/audit", params={"key": args.key, "environment": args.env})
    data = result.get("data", result) if isinstance(result, dict) else result
    if isinstance(data, list):
        for log in data:
            print(f"[{log.get('changeTime')}] {log.get('changeType'):<8} "
                  f"{log.get('configKey'):<30} by {log.get('operator')}")
    else:
        print(json.dumps(result, indent=2, ensure_ascii=False))

def cmd_health(args):
    try:
        result = api_request("GET", "/health")
        status = result.get("status", "unknown")
        redis = result.get("redis_available", False)
        print(f"服务状态: {status}")
        print(f"Redis: {'✅ 正常' if redis else '❌ 异常'}")
        print(f"缓存策略: {result.get('cache_strategy', 'unknown')}")
    except Exception as e:
        print(f"❌ 服务不可达: {e}", file=sys.stderr)
        sys.exit(1)

# ======================== CLI 入口 ========================

def main():
    parser = argparse.ArgumentParser(
        prog="cc-cli",
        description="Config Center 命令行工具",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
环境变量:
  CC_SERVER    Config Center 地址 (默认: http://localhost:8080)
  CC_USER      用户名 (默认: admin)
  CC_PASS      密码 (默认: admin)
  CC_ENV       环境 (默认: dev)

示例:
  cc-cli get app.name
  cc-cli list --env prod
  cc-cli set db.password secret123 --encrypted
  cc-cli import configs.json --env dev --overwrite
  cc-cli export --format yaml --output backup.yaml
  cc-cli watch --env dev
        """,
    )

    subparsers = parser.add_subparsers(dest="command", help="可用命令")

    # get
    p_get = subparsers.add_parser("get", help="获取单个配置")
    p_get.add_argument("key", help="配置键")
    p_get.add_argument("--env", default=ENV, help="环境")

    # list
    p_list = subparsers.add_parser("list", help="列出配置")
    p_list.add_argument("--env", default=ENV, help="环境")

    # set
    p_set = subparsers.add_parser("set", help="创建配置")
    p_set.add_argument("key", help="配置键")
    p_set.add_argument("value", help="配置值")
    p_set.add_argument("--desc", help="描述")
    p_set.add_argument("--encrypted", action="store_true", help="加密存储")
    p_set.add_argument("--env", default=ENV, help="环境")

    # update
    p_update = subparsers.add_parser("update", help="更新配置")
    p_update.add_argument("key", help="配置键")
    p_update.add_argument("value", help="新值")
    p_update.add_argument("--env", default=ENV, help="环境")

    # delete
    p_delete = subparsers.add_parser("delete", help="删除配置")
    p_delete.add_argument("key", help="配置键")
    p_delete.add_argument("--env", default=ENV, help="环境")

    # import
    p_import = subparsers.add_parser("import", help="批量导入")
    p_import.add_argument("file", help="JSON/YAML 文件路径")
    p_import.add_argument("--env", default=ENV, help="目标环境")
    p_import.add_argument("--overwrite", action="store_true", help="覆盖已有配置")

    # export
    p_export = subparsers.add_parser("export", help="批量导出")
    p_export.add_argument("--env", default=ENV, help="环境")
    p_export.add_argument("--format", choices=["json", "yaml"], default="json", help="格式")
    p_export.add_argument("--output", "-o", help="输出文件路径")

    # watch
    p_watch = subparsers.add_parser("watch", help="实时监听配置变更 (SSE)")
    p_watch.add_argument("--env", default=ENV, help="环境")

    # audit
    p_audit = subparsers.add_parser("audit", help="查询审计日志")
    p_audit.add_argument("--key", help="配置键过滤")
    p_audit.add_argument("--env", default=ENV, help="环境")

    # health
    subparsers.add_parser("health", help="检查服务健康状态")

    args = parser.parse_args()

    if not args.command:
        parser.print_help()
        sys.exit(1)

    commands = {
        "get": cmd_get,
        "list": cmd_list,
        "set": cmd_set,
        "update": cmd_update,
        "delete": cmd_delete,
        "import": cmd_import,
        "export": cmd_export,
        "watch": cmd_watch,
        "audit": cmd_audit,
        "health": cmd_health,
    }

    handler = commands.get(args.command)
    if handler:
        handler(args)
    else:
        parser.print_help()
        sys.exit(1)

if __name__ == "__main__":
    main()
