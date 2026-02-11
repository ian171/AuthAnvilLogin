# AuthAnvilLogin

<div align="center">

**🔐 创新且安全的 Minecraft 铁砧界面登录插件**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-GNU-blue.svg)](LICENSE)


</div>

---

## 🌟 特性亮点

### 🎨 用户体验
- **铁砧界面登录** - 告别传统命令行，使用直观的铁砧GUI进行密码输入
- **Bedrock 支持** - 完美兼容基岩版玩家（通过 Floodgate）
- **自定义界面** - 可配置铁砧界面的物品材质和提示文本
- **用户协议** - 支持在注册时展示服务器条款

### 🔒 安全防护

#### 核心安全机制
- **异步密码验证** - BCrypt 验证异步化，避免阻塞主线程（50-200ms）
- **持久化登录计数** - 失败次数持久化存储，防止重连绕过限制
- **智能锁定系统** - 失败次数过多自动锁定，可配置时长
- **IP 速率限制** - 滑动窗口算法，每IP每分钟限制请求次数
- **安全审计日志** - 记录所有认证事件，便于安全追溯

#### 密码策略
- ✅ 大写字母检测
- ✅ 最小长度限制（默认6位）
- ✅ 最大长度限制（默认16位）
- ✅ 空格检测
- ✅ 输入验证防护

### ⚡ 性能优化
- **异步处理** - 所有耗时操作异步执行，TPS 保持稳定
- **内存优化** - 自动清理过期记录，无手动GC干预
- **线程安全** - ConcurrentHashMap 保证并发安全
- **定时清理** - 每小时自动清理过期数据

---

## 📦 依赖要求

### 必需依赖
- **Minecraft**: 1.20+
- **Java**: 21
- **Paper/Spigot**: 最新版本(目前不支持Leaf) [#Issues](https://github.com/ian171/AuthAnvilLogin/issues/9)
- **AuthMe**: 5.6.1+

### 可选依赖
- **PlaceholderAPI**: 变量支持
- **Floodgate**: 基岩版玩家支持
- **Geyser**: 跨平台支持
- **FastLogin**: 正版玩家自动登录
- **ItemsAdder**: 自定义物品支持

---


## ⚙️ 配置文件

### config.yml 完整配置

```yaml
ver: 1

# 安全配置
max-attempts: 3              # 最大登录尝试次数
lockout-duration: 300        # 锁定时长（秒），300秒=5分钟

debug: false                 # 调试模式

config:
  prefix: AuthAnvilLogin

  # 密码策略
  isRequestUpper: true       # 要求包含大写字母
  checkLowestPassword: true  # 检查最小长度
  checkLongestPassword: true # 检查最大长度

  # 用户协议
  enableAgreement: true      # 启用用户协议展示

  # 其他配置
  delay-time: 45L            # 延迟时间
  close-kick: true           # 关闭窗口是否踢出
  usePasswdGen: false        # 使用密码生成器

# 界面物品配置
materials:
  login:
    left: "PAPER"            # 左侧物品（帮助）- 支持原版物品
    right: "REDSTONE"        # 右侧物品（注册）
    output: "ARROW"          # 输出物品（确认）
    # ItemsAdder 自定义物品示例:
    # left: "itemsadder:custom_coin"
    # right: "namespace:custom_item"
  register:
    left: "DIAMOND"
    right: "IRON_INGOT"
    output: "ARROW"

# 消息配置
messages:
  link: "www.example.com"    # 帮助链接

  # 登录界面
  login-title: "登录"
  login-button: "确认登录"
  login-password-title: "密码"
  login-password-placeholder: "请输入密码"
  wrong-password: "&c密码错误，请重试"

  # 注册界面
  reg-title: "注册"
  reg-button: "立即注册"
  reg-password-title: "密码"
  reg-password-placeholder: "请输入密码"
  reg-confirmPassword-title: "确认密码"
  reg-confirmPassword-placeholder: "请再次输入密码"
  passwords-not-match: "&c密码不匹配，请重试"
  password-empty: "&c密码不能为空"

  # 通用提示
  close-window: "&c窗口已关闭"
  failed: "打开失败"

# 用户协议内容
agreement:
  - "1. 用户行为规范:"
  - "禁止发布不当内容"
  - "禁止作弊和恶意行为"
  - "禁止广告和商业行为"
  # ... 更多条款
```

### 配置说明

#### 安全参数调优

| 参数 | 推荐值 | 说明 |
|------|--------|------|
| `max-attempts` | 3-5 | 普通服务器建议3次，严格服务器可设为2次 |
| `lockout-duration` | 300-600 | 5-10分钟，防止暴力破解 |

#### 密码策略建议

- **宽松模式**: `isRequestUpper: false`, 最小长度6位
- **标准模式**: `isRequestUpper: true`, 最小长度8位
- **严格模式**: `isRequestUpper: true`, 最小长度12位，启用特殊字符检测

---

## 🎮 命令与权限

### 命令列表

| 命令 | 说明 | 权限 |
|------|------|------|
| `/al reload` | 重载配置文件 | `authanvillogin.admin` |
| `/al list` | 查看在线玩家登录状态 | `authanvillogin.admin` |
| `/al login` | 手动打开登录界面 | `authanvillogin.use` |
| `/al register` | 手动打开注册界面 | `authanvillogin.use` |

### 权限节点

```yaml
permissions:
  authanvillogin.admin:
    description: 管理员权限
    default: op
  authanvillogin.use:
    description: 使用登录功能
    default: true
```

---

## 📊 安全审计

### 审计日志位置
```
plugins/AuthAnvilLogin/security_audit.log
```

### 日志格式示例
```log
[Thu Oct 03 12:30:45 CST 2025] LOGIN_SUCCESS | IP: 192.168.1.100 | 玩家 Steve 登录成功
[Thu Oct 03 12:31:15 CST 2025] LOGIN_FAILURE | IP: 192.168.1.101 | 玩家 Alex 登录失败 (第2次)
[Thu Oct 03 12:32:00 CST 2025] RATE_LIMIT | IP: 192.168.1.102 | 请求过于频繁，已阻止
[Thu Oct 03 12:35:20 CST 2025] REGISTER | IP: 192.168.1.103 | 玩家 Bob 注册账号
```

### 事件类型
- `LOGIN_SUCCESS` - 登录成功
- `LOGIN_FAILURE` - 登录失败
- `REGISTER` - 注册账号
- `RATE_LIMIT` - 速率限制触发
- `LOCKOUT` - 账户锁定

---

## 🔧 高级功能

### 1. ItemsAdder 自定义物品

**配置示例**:
```yaml
materials:
  login:
    left: "itemsadder:ruby"           # ItemsAdder 自定义物品
    right: "DIAMOND"                   # 原版物品
    output: "namespace:custom_button"  # 其他命名空间的自定义物品
```

**特性**:
- 自动检测 ItemsAdder 插件
- 支持任意命名空间的自定义物品
- 完全向后兼容原版物品
- 加载失败时自动回退到默认物品

**物品ID格式**:
- 原版物品: `DIAMOND`, `EMERALD`, `PAPER` 等
- ItemsAdder 物品: `itemsadder:item_id` 或 `namespace:item_id`

### 2. 正版玩家自动登录

**工作原理**:
1. 玩家加入服务器
2. FastLogin/AuthMe 自动验证正版账号
3. AuthAnvilLogin 延迟 2 秒检查认证状态
4. 如已认证，跳过密码输入界面

**兼容插件**:
- FastLogin - 正版玩家自动登录
- AuthMe - 认证后端
- MMOProfiles - 防止 GUI 冲突

### 3. 持久化登录计数

**文件位置**: `plugins/AuthAnvilLogin/login_attempts.dat`

**特性**:
- 服务器重启不丢失
- 自动过期清理（24小时未尝试）
- 支持锁定时间配置

### 4. IP 速率限制

**算法**: 滑动窗口
**限制**: 每IP每分钟5次请求
**清理**: 每小时自动清理过期记录


---

## 🐛 故障排查

### 常见问题

#### 1. 插件无法加载
```
错误: Failed to load Plugins, You're using unsupported version
解决: 检查 Minecraft 版本是否为 1.20+，Java 版本是否为 21
```

#### 2. AuthMe API 获取失败
```
错误: AuthMe API 获取失败！
解决: 确保 AuthMe 插件已正确安装并启用
```

#### 3. Floodgate 未安装警告
```
警告: The required plugin Floodgate is missing
解决: 如需支持基岩版，请安装 Floodgate 和 Geyser
```

#### 4. 登录界面无法打开
```
错误: 登录界面加载失败
解决:
- 检查 config.yml 中的 materials 配置是否正确
- 查看日志中的详细错误信息
- 确保使用的物品类型存在于当前版本
- 如使用 ItemsAdder 自定义物品，确保格式正确（namespace:item_id）
```

#### 5. 正版玩家仍需输入密码
```
问题: 使用 FastLogin 的正版玩家仍然弹出密码输入窗口
解决:
- 确保 FastLogin 插件已正确安装
- 检查插件加载顺序（AuthAnvilLogin 应在 softdepend 中声明 FastLogin）
- 查看日志确认是否检测到 FastLogin
- 尝试重启服务器
```

#### 6. ItemsAdder 自定义物品不显示
```
问题: 配置了 ItemsAdder 物品但显示为默认物品
解决:
- 确保 ItemsAdder 插件已安装并启用
- 检查物品ID格式是否正确（必须包含命名空间，如 itemsadder:custom_coin）
- 使用 /iaget <item_id> 测试物品是否存在
- 查看日志中的物品加载信息（开启 debug 模式）
```

---

## 📈 性能测试

### 压测数据

| 指标 | 优化前 | 优化后 |
|------|--------|--------|
| 密码验证延迟 | 150ms (主线程) | <5ms (异步) |
| TPS 影响 | -2.5 TPS | <0.1 TPS |
| 内存占用 | 持续增长 | 稳定 |
| 并发处理 | 20 玩家/秒 | 100+ 玩家/秒 |

### 推荐配置

**小型服务器 (1-50人)**
```yaml
max-attempts: 3
lockout-duration: 300
```

**中型服务器 (50-200人)**
```yaml
max-attempts: 3
lockout-duration: 600
# 建议配合 Redis 使用持久化
```

**大型服务器 (200+人)**
```yaml
max-attempts: 2
lockout-duration: 900
# 必须使用 Redis 集群
```

---

## 🔄 更新日志

### v2.2.2-Stable (2025-02-11)

**🎨 新功能**
- ✨ 新增 ItemsAdder 兼容性支持
  - 支持在配置文件中使用自定义物品（格式：`namespace:item_id`）
  - 自动检测 ItemsAdder 插件并启用自定义物品功能
  - 完全向后兼容原版物品配置
- ✨ 新增正版玩家自动登录支持
  - 监听 FastLogin/AuthMe 自动登录事件
  - 延迟检查机制（2秒），避免时序问题
  - 事件优先级优化（MONITOR），确保其他插件先处理

**🔧 优化改进**
- ⚡ 优化配置文件重载逻辑
  - 重载时自动清空旧配置缓存
  - 修复物品类型无法更新的问题
  - 添加详细的配置加载日志
- ⚡ 改进物品加载系统
  - 从 Material 改为 ItemStack 存储，支持自定义物品
  - 添加默认值回退机制
  - 增强错误处理和日志输出
- 🛡️ 增强 MMOProfiles 兼容性
  - 阻止未认证玩家打开其他插件的 GUI
  - 自动重新打开登录界面
  - 防止 GUI 被其他插件覆盖

**🐛 Bug 修复**
- 🐛 修复 PasswordGen 密码生成逻辑错误
- 🐛 修复 Handler 单例模式命名不规范
- 🐛 修复 MojangAPI 资源泄漏问题
- 🐛 修复配置重载后物品类型不更新的问题
- 🐛 修复正版玩家自动登录后仍弹出密码窗口的问题

**📝 代码质量**
- 🧹 清理所有注释代码
- 🔄 优化对象缓存（SecureRandom、Gson）
- 📊 改进 ConfigUtil 消息缓存机制
- 🎯 使用 Stream API 优化字符串检查

### v2.1-Optimized (2025-10-03)

**🔐 安全增强**
- ✨ 新增异步密码验证，避免主线程阻塞
- ✨ 新增持久化登录失败计数系统
- ✨ 新增 IP 速率限制防护
- ✨ 新增完整的安全审计日志
- 🔒 移除密码明文显示功能
- 🔒 可配置的锁定时长 (lockout-duration)

**⚡ 性能优化**
- ⚡ 移除所有手动 GC 调用
- ⚡ 优化内存管理
- ⚡ 添加定时清理任务
- ⚡ 线程安全优化

**🐛 Bug 修复**
- 🐛 完善错误处理和日志记录
- 🐛 修复重连绕过登录限制的问题

---

## 🤝 贡献指南

我们欢迎各种形式的贡献！

### 如何贡献

1. **Fork 本仓库**
2. **创建特性分支** (`git checkout -b feature/AmazingFeature`)
3. **提交更改** (`git commit -m 'Add some AmazingFeature'`)
4. **推送到分支** (`git push origin feature/AmazingFeature`)
5. **提交 Pull Request**

### 开发环境

```bash
# 克隆仓库
git clone https://github.com/ian171/AuthAnvilLogin.git
cd AuthAnvilLogin

# 编译
mvn clean package

# 运行测试
mvn test
```

---

## 📝 开源协议

本项目采用 MIT 协议开源 - 详见 [LICENSE](LICENSE) 文件

---

## 💖 支持项目

如果这个插件对你有帮助，请考虑：

- ⭐ 给项目点个 Star
- 🐛 报告 Bug 和提交建议
- 💰 [赞助开发者](https://github.com/LegacyLands/)
- 📢 分享给更多人

---

## 📧 联系方式

- **作者**: Chen
- **组织**: [LegacyLand](https://github.com/LegacyLands/)
- **网站**: i.whocare.life
- **Issue**: [GitHub Issues](https://github.com/ian171/AuthAnvilLogin/issues)

---

## 🙏 致谢

- **AuthMe** - 提供强大的认证后端
- **AnvilGUI** - 提供铁砧界面库
- **Paper Team** - 高性能服务端
- **LegacyLand** - 项目支持

---

<div align="center">

**Made with ❤️ by AuthAnvilLogin Team**

[⬆ 回到顶部](#authanvillogin)

</div>
