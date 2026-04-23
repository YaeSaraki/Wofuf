# 简化认证架构 - 变更摘要

## 变更时间
2026-04-09

## 变更目标

改进认证设计，使其更简单轻量，同时支持分布式和单体部署。

## 核心原则

1. **JWT 中验证用户身份**：userId, isAdmin, username
2. **权限控制下放到各模块**：各模块自己管理权限
3. **同时支持分布式和单体部署**
4. **保持简单轻量**

## 新增文件

### shared-auth 模块

1. **JwtUtils.kt**
   - 统一的 JWT 验证工具类
   - 验证 JWT 签名
   - 提取基本信息（userId, username, isAdmin）

2. **JwtAuthFilter.kt**
   - 统一的 JWT 认证过滤器
   - 设置基本认证信息
   - 提供静态方法获取用户信息

3. **JwtUserInfo.kt** (内联)
   - JWT 用户信息数据类
   - 支持序列化/反序列化

## 删除文件

1. **JwtValidator.kt** - 已被 JwtUtils 替代
2. **JwtAuthenticationFilter.kt** - 已被 JwtAuthFilter 替代

## 修改文件

### shared-auth 模块

1. **build.gradle.kts**
   - 添加 `spring-boot-starter-web` 依赖

### modules-users 模块

1. **JwtUserAuthenticationTokenFilter.kt**
   - 移除 `@Order` 注解
   - 添加 `@ConditionalOnProperty` 条件注解
   - 只在 `spring.auth.use-session-check=true` 时启用

2. **application.yml**
   - 添加 `auth.use-session-check` 配置项

### modules-forum 模块

1. **PermissionAspect.kt**
   - 完全重构
   - 使用 `JwtAuthFilter` 的静态方法
   - 简化权限检查逻辑

2. **ForumSecurityConfig.kt**
   - 使用 `JwtAuthFilter` 替代 `JwtAuthenticationFilter`

3. **ForumApplication.kt**
   - 移除 `excludeFilters` 配置

## 架构改进

### 之前的问题

1. **复杂的 filter 优先级**：需要 `@Order` 注解控制执行顺序
2. **依赖 details 属性**：不同 filter 设置的 details 类型不同
3. **重复的验证逻辑**：JwtValidator 和 JwtUserAuthenticationTokenFilter 都在验证 JWT

### 优化后的架构

```
JwtUtils (验证 JWT) → JwtAuthFilter (设置认证) → SecurityContext
                                                     ↓
                                            PermissionAspect (检查权限)
```

### 关键改进

1. **统一 JWT 验证**：所有模块使用 `JwtUtils`
2. **统一认证入口**：所有模块使用 `JwtAuthFilter`
3. **简化权限检查**：直接从 `JwtAuthFilter` 静态方法获取信息
4. **条件加载**：用户模块 filter 通过配置控制是否加载

## 配置说明

### 用户模块（必需）

```yaml
auth:
  jwt:
    secret: your-secret-key-must-be-at-least-32-bytes-long-and-secure!
    expiration: 3600000
    refresh-expiration: 604800000
    issuer: auth-service
    clock-skew: 60
    token-version: '0'

  # 是否使用 Redis 会话检查
  use-session-check: true

  redis:
    host: localhost
    port: 6380
    password: ""
    database: 0
```

### 论坛模块（可选）

```yaml
auth:
  jwt:
    secret: your-secret-key-must-be-at-least-32-bytes-long-and-secure!
    expiration: 3600000
    refresh-expiration: 604800000
    issuer: auth-service
    clock-skew: 60
    token-version: '0'
```

## 使用示例

### 获取当前用户信息

```kotlin
// 获取用户 ID
val userId = JwtAuthFilter.getCurrentUserId()

// 检查是否为管理员
val isAdmin = JwtAuthFilter.isAdmin()

// 检查是否已登录
val isAuthenticated = JwtAuthFilter.isAuthenticated()

// 获取完整用户信息
val userInfo = JwtAuthFilter.getCurrentUser()
```

### 权限检查

```kotlin
@Aspect
@Component
class PermissionAspect {

    @Around("@annotation(requirePermission)")
    fun checkPermission(...): Any {
        // 1. 检查是否已登录
        if (!JwtAuthFilter.isAuthenticated()) {
            throw PermissionDeniedException("用户未登录")
        }

        // 2. 检查是否为系统管理员
        if (JwtAuthFilter.isAdmin()) {
            return joinPoint.proceed()
        }

        // 3. 检查模块级权限
        val userId = JwtAuthFilter.getCurrentUserId()
        val member = memberRepo.findMemberByUserId(userId)
        if (!member.hasPermission(permission)) {
            throw PermissionDeniedException("权限不足")
        }

        return joinPoint.proceed()
    }
}
```

## 部署模式

### 单体部署

```
单体应用
  ├─ JwtUtils
  ├─ JwtAuthFilter
  ├─ 用户模块 Filter (可选，配置 use-session-check=true)
  └─ 各模块 PermissionAspect
```

### 分布式部署

```
用户模块
  ├─ JwtUserAuthFilter (验证 Redis)
  └─ 会话管理

其他模块
  ├─ JwtAuthFilter (验证签名)
  └─ PermissionAspect (检查权限)
```

## 验证

✅ 编译成功
✅ 构建成功
✅ 支持单体部署
✅ 支持分布式部署
✅ 配置简单
✅ 代码简洁

## 优势总结

1. **简单轻量**
   - 统一的 JWT 验证逻辑
   - 最小化依赖
   - 易于理解和维护

2. **灵活扩展**
   - 各模块独立管理权限
   - 不影响其他模块
   - 易于添加新模块

3. **部署友好**
   - 同时支持单体和分布式
   - 配置简单
   - 无需复杂配置

4. **性能优化**
   - JWT 本地验证，无网络调用
   - 权限检查在本地数据库
   - 减少 Redis 依赖

## 文档

- `.document/SIMPLE-AUTH-ARCHITECTURE.md` - 详细架构文档

## 联系方式

- 作者：YaeSaraki
- 邮箱：ikaraswork@iCloud.com
