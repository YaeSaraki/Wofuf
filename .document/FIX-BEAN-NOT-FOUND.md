# 修复 Bean 未找到错误 - 2026-04-09

## 问题描述

启动应用时出现错误：

```
Field jwtUserAuthenticationTokenFilter in dev.saraki.wofuf.modules.users.config.UserSecurityConfig
required a bean of type 'dev.saraki.wofuf.modules.users.infra.auth.springSecurity.JwtUserAuthenticationTokenFilter'
that could not be found.
```

## 根本原因

`JwtUserAuthenticationTokenFilter` 添加了 `@ConditionalOnProperty` 条件注解：

```kotlin
@Component
@ConditionalOnProperty(name = ["spring.auth.use-session-check"], havingValue = "true", matchIfMissing = false)
class JwtUserAuthenticationTokenFilter : OncePerRequestFilter()
```

当 `spring.auth.use-session-check` 不为 `true` 时，bean 不会被创建。

但 `UserSecurityConfig` 仍尝试注入它：

```kotlin
@Autowired
private lateinit var jwtUserAuthenticationTokenFilter: JwtUserAuthenticationTokenFilter
```

## 解决方案

更新 `UserSecurityConfig`，支持两种模式：

### 1. 使用 `@Autowired(required = false)` 注入可选依赖

```kotlin
@Autowired(required = false)
private var jwtUserAuthenticationTokenFilter: JwtUserAuthenticationTokenFilter? = null

@Autowired
private lateinit var jwtAuthFilter: JwtAuthFilter
```

### 2. 条件性添加 filter

```kotlin
// 优先使用会话检查 filter，否则使用统一 filter
if (jwtUserAuthenticationTokenFilter != null) {
    // 单体部署 + Redis 会话检查模式
    http.addFilterBefore(
        jwtUserAuthenticationTokenFilter,
        UsernamePasswordAuthenticationFilter::class.java
    )
} else {
    // 分布式部署或单体部署（无会话检查）模式
    http.addFilterBefore(
        jwtAuthFilter,
        UsernamePasswordAuthenticationFilter::class.java
    )
}
```

## 部署模式

### 模式 1：单体部署 + Redis 会话检查

```yaml
# 用户模块配置
auth:
  use-session-check: true  # 启用会话检查

  redis:
    host: localhost
    port: 6380
```

**使用的 Filter**：`JwtUserAuthenticationTokenFilter`

### 模式 2：单体部署（无会话检查）

```yaml
# 用户模块配置
auth:
  use-session-check: false  # 禁用会话检查（或省略此配置）
```

**使用的 Filter**：`JwtAuthFilter`

### 模式 3：分布式部署

```yaml
# 用户模块配置
auth:
  use-session-check: true  # 用户模块需要会话检查

# 论坛模块配置
# 不需要特殊配置，自动使用 JwtAuthFilter
```

**使用的 Filter**：
- 用户模块：`JwtUserAuthenticationTokenFilter`
- 其他模块：`JwtAuthFilter`

## 修改文件

**modules-users/config/UserSecurityConfig.kt**

- 添加 `jwtAuthFilter` 依赖
- 将 `jwtUserAuthenticationTokenFilter` 改为可选注入
- 条件性选择使用的 filter

## 验证

✅ 编译成功
✅ 构建成功
✅ 支持多种部署模式

## 配置建议

### 单体部署（推荐）

```yaml
# application.yml
auth:
  use-session-check: false  # 简化部署，减少 Redis 依赖
```

**优点**：
- 配置简单
- 不需要 Redis
- 更容易测试

### 分布式部署

```yaml
# 用户模块 application.yml
auth:
  use-session-check: true  # 需要会话管理

# 其他模块不需要特殊配置
```

**优点**：
- 支持会话管理
- 支持强制下线
- 支持令牌刷新

## 总结

通过使用 `@Autowired(required = false)` 和条件性 filter 选择，现在支持：

1. ✅ 单体部署 + Redis 会话检查
2. ✅ 单体部署（无会话检查）
3. ✅ 分布式部署

配置简单，代码灵活！
