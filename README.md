# AuthAnvilLogin
This is a plugin for Minecraft server, which can be used to login with AnvilGui

**欢迎来到 AuthAnvilLogin**
这是一个`创新且安全`的 Minecraft 插件，它利用铁砧界面为玩家提供独特的登录体验。通过 **AuthAnvilLogin**，玩家再也不用输入指令了

## 功能特性

- 铁砧登录界面：AuthAnvilLogin 利用铁砧界面让玩家输入密码进行登录，这一设计不仅新颖，而且能够提供更好的用户体验。

- 密码安全性：插件支持多种密码策略，包括检查密码是否包含大写字母、是否满足最短密码要求以及是否满足最长密码要求。

- 配置灵活：通过 config.yml 文件，管理员可以轻松配置登录尝试的最大次数、密码策略等设置，以满足不同服务器的需求。
Config实例：
```
max-attempts: 3
config:
    isRequestUpper: true
    checkLowestPassword: true
    checkLongestPassword: true
```

权限控制：插件提供了详细的权限控制，确保只有拥有相应权限的玩家才能使用特定的命令和功能。

依赖 AuthMe：AuthAnvilLogin 依赖于 AuthMe 插件，确保账户登录的安全性。

通过以上功能特性，AuthAnvilLogin 插件为 Minecraft 服务器提供了一个安全、灵活且用户友好的登录解决方案。

## Special:

Supported by [LegacyLand](https://github.com/LegacyLands/)
Please Donate it!
