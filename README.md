# ProAuthMeLogin

AuthMe 的铁砧/Dialog 登录 GUI 插件，支持 Paper、Folia、Purpur。

## 依赖

| 依赖 | 类型        |
|------|-----------|
| Paper 1.21+ / Folia | ✅必须       |
| Java 21 | ✅必须       |
| AuthMe 5.6.1+ | ✅必须       |
| Floodgate + Geyser | ⚠️可选（基岩版支持） |
| ItemsAdder | ⚠️可选（自定义物品） |
| FastLogin | ⚠️可选（正版自动登录） |
| PlaceholderAPI | ⚠️可选        |

## 功能

- **双 GUI 模式**：1.21.5+ 客户端自动使用原生 Dialog 窗口，其余客户端回退至铁砧 GUI
- **用户协议弹窗**：登录/注册完成后展示协议，拒绝则踢出服务器
- **基岩版支持**：通过 Floodgate 自动识别并路由至基岩版 GUI
- **安全防护**：登录失败锁定、IP 速率限制、安全审计日志
- **密码策略**：可配置长度限制、大写字母要求
- **Folia 兼容**：全部调度操作适配 Folia 多线程

## 配置

```yaml
max-attempts: 3           # 最大登录尝试次数
lockout-duration: 300     # 锁定时长（秒）

config:
  isRequestUpper: true    # 密码须含大写字母
  checkLowestPassword: true
  checkLongestPassword: true
  enableAgreement: true   # 启用用户协议弹窗
  useDialogGui: true      # 启用 Dialog GUI（1.21.5+ 客户端）
  close-kick: true        # 强制无法关闭登录界面

materials:
  login:
    left: "PAPER"
    right: "REDSTONE"
    output: "ARROW"
  register:
    left: "DIAMOND"
    right: "IRON_INGOT"
    output: "ARROW"

messages:
  login-title: "Login"
  reg-title: "Register"
  # ... 其余见默认 config.yml

agreement:
  - "1. 禁止作弊"
  - "2. 遵守服务器规则"
  # ... 自定义协议内容
```

> 物品支持 ItemsAdder 格式：`namespace:item_id`

## 命令

| 命令             | 说明       | 权限                     |
|----------------|----------|------------------------|
| `/al reload`   | 重载配置     | `authanvillogin.admin` |
| `/al list`     | 查看玩家认证状态 | `authanvillogin.admin` |
| `/al login`    | 手动打开登录界面 | 所有人                    |
| `/al register` | 手动打开注册界面 | 所有人                    |

## 常见问题

**登录界面无法打开**：检查 `materials` 配置中的物品名称是否有效。

**正版玩家仍需输入密码**：确认 FastLogin 与 AuthMe 均正常运行，插件会延迟 2 秒等待自动登录完成。

**Folia 报错 UnsupportedOperationException**：升级至最新版本，旧版本存在使用 BukkitScheduler 的问题已修复。

**getClientBrandName() NullPointerException**：升级至最新版本，已加入空值保护。

## 协议

GPL-3.0 license © 
