# 密码找回（安全问题）功能设计

**日期：** 2026-05-23  
**插件：** ProAuthMeLogin  
**状态：** 已审批

---

## 1. 功能概述

为 AuthMeReloaded 新增基于安全问题的密码找回功能，支持：

- 注册完成后引导玩家设置安全问题（可跳过）
- 玩家登录失败达到上限或手动执行 `/al forgot` 触发找回流程
- 管理员命令 `/al resetpw <玩家名> <新密码>` 强制重置
- 全部交互通过 Paper 1.21.7+ Dialog GUI 实现（低版本回退提示）

---

## 2. 架构概览

### 新增组件

| 组件 | 类型 | 职责 |
|---|---|---|
| `SecurityQuestionManager` | 单例 Manager | 安全问题的增删查验，锁定管理 |
| `security_questions.yml` | 运行时数据文件 | 存储玩家问题索引与答案哈希 |
| `qsetting.yml` | 配置文件（随插件分发） | 预设题目列表、功能开关、最大失败次数 |

### 扩展的现有组件

| 组件 | 扩展内容 |
|---|---|
| `Handler` | 新增 `openForgotPasswordDialog()`、`openSetQuestionDialog()`、`openNewPasswordDialog()` |
| `AccountSettingCommand` | 新增 `/al forgot` 子命令、`/al resetpw` 管理员命令 |
| `Handler.handleLogin()` | 登录失败达到上限时触发找回入口 |
| `Handler.handleRegistry()` | 注册成功后在 `sendAgreement()` 之后触发设置问题引导 |

---

## 3. 配置文件设计

### `qsetting.yml`（随插件分发，服主编辑）

```yaml
enabled: true
max-answer-attempts: 3
questions:
  - "你最喜欢的宠物名字是？"
  - "你小学就读的学校名称？"
  - "你父母的生日是？"
  - "你第一个好友的名字是？"
  - "你最喜欢的游戏是？"
```

### `security_questions.yml`（运行时自动生成，插件管理）

```yaml
PlayerName:
  question-index: 2
  answer-hash: "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd..."  # SHA-256
  failed-attempts: 0
  locked-until: 0   # Unix 时间戳（毫秒），0 = 未锁定
```

---

## 4. Dialog 交互流程

### 流程 A：注册后设置安全问题

```
注册成功
  └→ sendAgreement()（已有）
       └→ openSetQuestionDialog()
            ├── 展示 qsetting.yml 中的题目（body 区域显示编号列表，两个输入框：数字框填题目编号，文本框填答案）
            ├── [确认设置] → SecurityQuestionManager.setQuestion() → 提示"设置成功"
            └── [跳过] → 提示"之后可通过 /al account 设置"
```

### 流程 B：找回密码（玩家触发）

```
/al forgot  或  登录失败达到 MAX_ATTEMPTS 上限
  └→ openForgotPasswordDialog()
       ├── 未设置安全问题 → 提示"未设置安全问题，请联系管理员" → 结束
       ├── 已锁定 → 提示剩余锁定时间 → 结束
       └→ 展示玩家已绑定的题目 + 答案输入框
            ├── [提交]
            │     ├── 验证通过 → openNewPasswordDialog()
            │     │     └→ 输入新密码 → AuthMeApi.changePassword() → 提示"重置成功，请重新登录"
            │     └── 验证失败 → 记录失败次数
            │           ├── 未达上限 → 提示剩余次数 → 重新弹出
            │           └── 达到上限 → 锁定 5 分钟 → 提示"请联系管理员"
            └── [取消] → 关闭
```

### 流程 C：管理员命令重置

```
/al resetpw <玩家名> <新密码>
  └→ 检查权限 authanvillogin.admin
       └→ AuthMeApi.changePassword()
            └→ SecurityQuestionManager.clearLock() — 清除锁定记录
                 └→ 向管理员反馈结果
```

---

## 5. SecurityQuestionManager 接口设计

```java
// 设置安全问题
void setQuestion(String playerName, int questionIndex, String rawAnswer)

// 是否已设置
boolean hasQuestion(String playerName)

// 验证答案（含失败计数与锁定逻辑）
// 返回：CORRECT / WRONG / LOCKED
VerifyResult verifyAnswer(String playerName, String rawAnswer)

// 管理员清除玩家的问题与锁定
void resetQuestion(String playerName)

// 清除锁定（保留问题）
void clearLock(String playerName)

// 检查是否锁定
boolean isLocked(String playerName)

// 获取玩家已绑定的题目文本
String getQuestion(String playerName)

// 加载 / 保存 security_questions.yml
void load()
void save()
```

---

## 6. 安全策略

| 项目 | 策略 |
|---|---|
| 答案哈希 | `SHA-256(answer.toLowerCase().trim())` |
| 锁定触发 | 连续失败 `max-answer-attempts` 次 |
| 锁定时长 | 5 分钟（固定，不可配置，防止暴力枚举） |
| 管理员解锁 | `/al resetpw` 自动清除锁定 |
| 低版本兼容 | 协议版本 < 772 时提示"请联系管理员重置密码" |

---

## 7. 新增权限

| 权限节点 | 说明 | 默认 |
|---|---|---|
| `authanvillogin.admin` | 已有，覆盖 `/al resetpw` | op |

无需新增权限节点。

---

## 8. 不在范围内

- 邮件验证
- 多重安全问题
- 玩家自定义问题内容（仅预设题目）
- 低版本铁砧 GUI 实现找回流程（仅提示联系管理员）
