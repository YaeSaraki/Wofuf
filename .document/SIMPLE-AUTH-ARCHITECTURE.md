# 简化的认证架构设计

## 概述

本设计遵循以下原则：
1. **JWT 中验证用户身份**：userId, isAdmin, username
2. **权限控制下放到各模块**：各模块自己管理权限
3. **同时支持分布式和单体部署**
4. **保持简单轻量**

## 架构设计

### 认证分层

```
┌─────────────────────────────────────────────────────────────┐
│                     JWT Token                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ userId: "user-123"                               │   │
│  │ username: "YaeSaraki"                            │   │
│  │ isAdmin: true                                    │   │
│  │ jti: "session-id"                                │   │
│  │ exp: 1680903600                                  │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
            ┌───────────────┴───────────────┐
            │                               │
    ┌───────▼────────┐          ┌────────▼────────┐
    │ 系统级权限      │          │ 模块级权限      │
    │ isAdmin         │          │ 论坛权限点       │
    │ JWT 验证       │          │ 本地数据库查询   │
    └────────────────┘          └─────────────────┘
```

## 核心组件

### 1. JwtUtils（共享模块）

**职责**：
- 验证 JWT 签名
- 提取 JWT 中的基本信息

**特点**：
- 不查询 Redis
- 不验证会话状态
- 轻量级，无依赖

```kotlin
@Component
class JwtUtils(private val jwtConfig: JwtConfig) {

    fun verifyToken(token: String): JwtUserInfo? {
        // 验证签名并提取基本信息
    }
}

data class JwtUserInfo(
    val userId: String,
    val username: String,
    val isAdmin: Boolean
)
```

### 2. JwtAuthFilter（共享模块）

**职责**：
- 从请求头获取 JWT Token
- 验证 JWT 签名
- 设置基本的认证信息

**特点**：
- 统一的认证入口
- 支持 all 模块使用
- 简单轻量

```kotlin
@Component
class JwtAuthFilter : OncePerRequestFilter() {

    override fun doFilterInternal(...) {
        val userInfo = jwtUtils.verifyToken(token)

        if (userInfo != null) {
            val authentication = UsernamePasswordAuthenticationToken(
                userInfo.userId,
                null,
                if (userInfo.isAdmin) {
                    AuthorityUtils.createAuthorityList("ROLE_ADMIN")
                } else {
                    AuthorityUtils.NO_AUTHORITIES
                }
            )
            authentication.details = userInfo.toJson()
            SecurityContextHolder.getContext().authentication = authentication
        }
    }

    companion object {
        fun getCurrentUserId(): String?
        fun isAdmin(): Boolean
        fun isAuthenticated(): Boolean
    }
}
```

### 3. PermissionAspect（各模块自己实现）

**职责**：
- 检查用户是否已登录
- 检查是否为系统管理员
- 检查是否有模块级权限

**示例（论坛模块）**：

```kotlin
@Aspect
@Component
class PermissionAspect {

    @Around("@annotation(requirePermission)")
    fun checkPermission(joinPoint: ProceedingJoinPoint, requirePermission: RequirePermission): Any {
        // 1. 检查是否已登录
        if (!JwtAuthFilter.isAuthenticated()) {
            throw PermissionDeniedException("用户未登录")
        }

        // 2. 检查是否为系统管理员
        if (JwtAuthFilter.isAdmin()) {
            return joinPoint.proceed()
        }

        // 3. 检查模块级权限
        val member = memberRepo.findMemberByUserId(userId)
        if (!member.hasPermission(requirePermission.permission)) {
            throw PermissionDeniedException("权限不足")
        }

        return joinPoint.proceed()
    }
}
```

## 部署模式

### 单体部署

```
┌─────────────────────────────────────────────────────────────┐
│                   单体应用 (Port 8005)                    │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐   │
│  │           shared-auth 模块                          │   │
│  │  ┌────────────────┐  ┌────────────────────────┐     │   │
│  │  │   JwtUtils    │  │   JwtAuthFilter      │     │   │
│  │  └────────────────┘  └────────────────────────┘     │   │
│  └───────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐   │
│  │           modules-users 模块                       │   │
│  │  (可选：当 spring.auth.use-session-check=true 时)    │   │
│  │  ┌───────────────────────────────────────┐            │   │
│  │  │ JwtUserAuthenticationTokenFilter     │            │   │
│  │  │ (验证 Redis 会话)                     │            │   │
│  │  └───────────────────────────────────────┘            │   │
│  └───────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐   │
│  │           modules-forum 模块                       │   │
│  │  ┌───────────────────────────────────────┐            │   │
│  │  │     ForumSecurityConfig               │            │   │
│  │  │     PermissionAspect                 │            │   │
│  │  │     (检查论坛权限)                    │            │   │
│  │  └───────────────────────────────────────┘            │   │
│  └───────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

**配置**：
```yaml
# 用户模块配置
auth:
  use-session-check: true  # 启用 Redis 会话检查

# 论坛模块配置
# 不需要特殊配置，直接使用 JwtAuthFilter
```

### 分布式部署

```
┌──────────────────────┐     ┌──────────────────────┐
│   用户模块 (8001)   │     │   论坛模块 (8003)   │
│                      │     │                      │
│  ┌────────────────┐  │     │  ┌────────────────┐  │
│  │ JwtUserAuth   │  │     │  │ JwtAuthFilter │  │
│  │ Filter        │  │     │  │ (验证签名)     │  │
│  │ (验证 Redis)   │  │     │  └────────────────┘  │
│  └────────────────┘  │     │                      │
└──────────────────────┘     │  ┌────────────────┐  │
                            │  │ PermissionAspect│  │
                            │  │ (检查论坛权限)  │  │
                            │  └────────────────┘  │
                            └──────────────────────┘
```

**配置**：
```yaml
# 用户模块配置
auth:
  use-session-check: true  # 启用 Redis 会话检查

# 论坛模块配置
# 不需要特殊配置，直接使用 JwtAuthFilter
```

## 配置说明

### JWT 配置（所有模块必须一致）

```yaml
auth:
  jwt:
    secret: your-secret-key-must-be-at-least-32-bytes-long-and-secure!
    expiration: 3600000  # 1小时
    refresh-expiration: 604800000  # 7天
    issuer: auth-service
    clock-skew: 60  # 60秒时钟偏差
    token-version: '0'
```

### 用户模块配置

```yaml
auth:
  use-session-check: true  # 启用 Redis 会话检查

  redis:
    host: localhost
    port: 6380
    password: ""
    database: 0
```

### 论坛模块配置

```yaml
# 不需要特殊配置
# JwtAuthFilter 会自动被扫描和使用
```

## 使用示例

### 1. 用户登录

```bash
POST http://localhost:8005/api/v1/users/login
Content-Type: application/json

{
  "username": "YaeSaraki",
  "password": "password"
}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "expiresIn": 3600000
  }
}
```

### 2. 调用受保护接口

```bash
# 系统管理员接口
POST http://localhost:8005/api/v1/forum/admin/posts/{postId}/feature
Content-Type: application/json
MeoKey: your-jwt-token

# 普通用户接口（需要权限）
POST http://localhost:8005/api/v1/forum/posts
Content-Type: application/json
MeoKey: your-jwt-token
```

## 权限控制流程

### 系统管理员接口

```
请求 → JwtAuthFilter
       ├─ 验证 JWT 签名
       └─ 设置 ROLE_ADMIN
           ↓
       PermissionAspect
       ├─ 检查是否已登录 ✓
       └─ 检查是否为系统管理员 ✓
           ↓
       允许访问
```

### 模块级权限接口

```
请求 → JwtAuthFilter
       ├─ 验证 JWT 签名
       └─ 设置 userId
           ↓
       PermissionAspect
       ├─ 检查是否已登录 ✓
       ├─ 检查是否为系统管理员 ✗
       └─ 检查模块级权限
           ├─ 查询本地数据库
           └─ 检查权限点 ✓
               ↓
           允许访问
```

## 优势

1. **简单轻量**：
   - 统一的 JWT 验证逻辑
   - 最小化依赖
   - 易于理解和维护

2. **灵活扩展**：
   - 各模块独立管理权限
   - 不影响其他模块
   - 易于添加新模块

3. **部署友好**：
   - 同时支持单体和分布式
   - 配置简单
   - 无需复杂配置

4. **性能优化**：
   - JWT 本地验证，无网络调用
   - 权限检查在本地数据库
   - 减少 Redis 依赖

## 安全考虑

1. **JWT 密钥管理**：
   - 所有模块使用相同密钥
   - 生产环境使用环境变量注入
   - 定期轮换密钥

2. **权限最小化**：
   - 默认无权限
   - 显式授权
   - 定期审计

3. **会话管理**：
   - 用户模块负责会话管理（Redis）
   - 支持强制下线
   - 支持令牌刷新

## 迁移指南

### 从旧架构迁移

1. **更新依赖**：
   ```kotlin
   implementation(project(":shared-auth"))
   ```

2. **更新配置**：
   ```yaml
   auth:
     jwt:
       secret: your-secret-key
   ```

3. **更新代码**：
   ```kotlin
   // 旧代码
   val tokenInfo = authentication.details as? JwtTokenInfo

   // 新代码
   val userId = JwtAuthFilter.getCurrentUserId()
   val isAdmin = JwtAuthFilter.isAdmin()
   ```

### 新模块集成

1. **添加依赖**：
   ```kotlin
   implementation(project(":shared-auth"))
   ```

2. **配置 Security**：
   ```kotlin
   @Autowired
   private lateinit var jwtAuthFilter: JwtAuthFilter

   @Bean
   fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
       http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
       return http.build()
   }
   ```

3. **实现权限检查**：
   ```kotlin
   @Aspect
   @Component
   class PermissionAspect {
       @Around("@annotation(requirePermission)")
       fun checkPermission(...): Any {
           if (!JwtAuthFilter.isAuthenticated()) {
               throw PermissionDeniedException("用户未登录")
           }
           // 检查权限...
       }
   }
   ```

## 总结

这个简化的认证架构：

✅ **JWT 中验证用户身份**：userId, isAdmin, username
✅ **权限控制下放到各模块**：各模块自己管理权限
✅ **同时支持分布式和单体部署**：配置简单
✅ **保持简单轻量**：最小依赖，易于维护
