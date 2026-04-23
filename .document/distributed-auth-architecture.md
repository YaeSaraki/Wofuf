# 分布式认证系统架构文档

## 概述

Wofuf 采用分布式微服务架构，认证系统按照以下原则设计：

- **用户模块** 负责认证和授权（JWT 签发、会话管理、权限验证）
- **其他模块**（如论坛）只调用用户模块的验证服务或验证 JWT 签名
- 各模块部署在不同机器上，拥有独立的数据库

## 架构设计

### 1. 认证流程

#### 用户登录流程

```
Client → User Module
  ├─ 1. 验证用户凭证
  ├─ 2. 生成 JWT Token（包含 userId, username, isAdminUser, jti, tokenVersion）
  ├─ 3. 将 session 存入 Redis（jti → userId）
  └─ 4. 返回 JWT Token 给客户端
```

#### 下游服务验证流程

```
Client → Downstream Service (e.g., Forum)
  ├─ 1. 从 Header 获取 JWT Token (MeoKey)
  ├─ 2. 使用 JwtValidator 验证 JWT 签名
  ├─ 3. 从 JWT 中提取用户信息（userId, isAdminUser）
  └─ 4. 设置 SecurityContext
```

#### 权限验证流程

```
PermissionAspect
  ├─ 1. 检查用户是否为管理员（从 JWT 的 isAdminUser）
  ├─ 2. 如果是管理员，直接通过
  └─ 3. 如果不是管理员，查询本地 Member 数据库检查权限点
```

### 2. 组件说明

#### shared-auth 模块

**JwtValidator**
- 统一的 JWT 验证器
- 只验证 JWT 签名，不查询 Redis
- 供所有微服务使用

**JwtAuthenticationFilter**
- 统一的 JWT 认证过滤器
- 供所有下游微服务使用
- 从 JWT Token 中提取用户信息并设置到 SecurityContext

#### 用户模块 (modules-users)

**UserAuthService**
- 用户登录
- JWT 签发
- 会话管理（Redis）
- JWT 验证（包含 Redis 会话检查）

**JwtUserAuthenticationTokenFilter**
- 用户模块专用过滤器
- 验证 JWT 签名 + Redis 会话

#### 论坛模块 (modules-forum)

**ForumSecurityConfig**
- 使用共享的 JwtAuthenticationFilter
- 配置路由权限

**PermissionAspect**
- AOP 切面，验证用户权限
- 优先使用 JWT 中的 isAdminUser 标识
- 非管理员查询本地 Member 数据库

### 3. 数据流转

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Client    │────────>│   Gateway   │────────>│  Downstream │
└─────────────┘         └─────────────┘         │   Service   │
                                                      │
                                                      ▼
                                              ┌─────────────┐
                                              │JwtValidator │
                                              └─────────────┘
                                                      │
                                                      ▼
                                              ┌─────────────┐
                                              │  Security  │
                                              │  Context   │
                                              └─────────────┘
```

### 4. 部署架构

```
┌─────────────────────────────────────────────────────────────┐
│                        Gateway                             │
│                    (Port: 9999)                            │
└──────────────┬──────────────────┬────────────────────────┘
               │                  │
               ▼                  ▼
┌─────────────────────┐  ┌─────────────────────┐
│   User Module       │  │   Forum Module      │
│   (Port: 8001)      │  │   (Port: 8003)      │
│                     │  │                     │
│  - 用户认证         │  │  - 帖子管理         │
│  - JWT 签发         │  │  - 评论管理         │
│  - 会话管理 (Redis) │  │  - JWT 验证         │
│  - 用户数据库       │  │  - 论坛数据库       │
└─────────────────────┘  └─────────────────────┘

┌─────────────────────┐  ┌─────────────────────┐
│   Eureka Server     │  │   Redis Cluster     │
│   (Port: 8761)     │  │   (Port: 6380)     │
└─────────────────────┘  └─────────────────────┘

┌─────────────────────┐
│   MySQL Cluster     │
│   (Port: 3307)     │
└─────────────────────┘
```

## 配置说明

### JWT 配置（所有服务必须相同）

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

### 服务发现配置（所有服务）

```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://eureka-server:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
```

### 网关路由配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: modules-users
          uri: lb://modules-users
          predicates:
            - Path=/api/v1/users/**

        - id: modules-forum
          uri: lb://modules-forum
          predicates:
            - Path=/api/v1/forum/**

        - id: modules-players
          uri: lb://modules-players
          predicates:
            - Path=/api/v1/players/**
```

## 使用示例

### 1. 用户登录

```bash
POST http://gateway:9999/api/v1/users/login
Content-Type: application/json

{
  "username": "YaeSaraki",
  "password": "password"
}

Response:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "expiresIn": 3600000
}
```

### 2. 调用受保护接口

```bash
POST http://gateway:9999/api/v1/forum/posts
Content-Type: application/json
MeoKey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "title": "测试帖子",
  "content": "这是测试内容"
}
```

### 3. 管理员接口调用

```bash
POST http://gateway:9999/api/v1/forum/admin/posts/{postId}/review
Content-Type: application/json
MeoKey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 安全考虑

### 1. JWT 密钥管理

- 生产环境必须使用强密钥（至少 32 字节）
- 密钥应通过环境变量或密钥管理服务注入
- 不同环境使用不同密钥

### 2. 会话管理

- 用户模块负责会话管理（Redis）
- JWT Token 包含会话 ID (jti)
- 支持强制下线（通过递增 tokenVersion）

### 3. 权限控制

- 管理员权限通过 JWT 的 isAdminUser 标识
- 普通用户权限通过各模块的本地数据库检查
- 支持细粒度的权限点控制

### 4. 令牌刷新

- 使用 Refresh Token 机制
- 支持令牌轮换（Rotation）
- 旧 Refresh Token 立即失效

## 故障排查

### 问题：无效的认证信息

**原因**：
- JWT Token 格式错误
- JWT 签名验证失败
- JWT Token 已过期

**解决**：
1. 检查 JWT 密钥是否一致
2. 检查 Token 是否在有效期内
3. 检查 Token 格式是否正确

### 问题：权限被拒绝

**原因**：
- 用户未登录
- 用户权限不足
- 本地数据库中用户信息不存在

**解决**：
1. 检查 Token 是否有效
2. 检查用户是否为管理员
3. 检查本地数据库中是否有对应的 Member 记录

## 扩展建议

### 1. 添加 OpenFeign 客户端

如果需要下游服务调用用户模块获取更多用户信息：

```kotlin
@FeignClient(name = "modules-users")
interface UserAuthClient {
    @GetMapping("/api/v1/users/me")
    fun getCurrentUser(): UserDto
}
```

### 2. 添加 API Gateway 认证过滤器

在网关层统一验证 JWT：

```kotlin
@Component
class GatewayAuthFilter : GlobalFilter {
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        // 验证 JWT
        // 添加用户信息到请求头
    }
}
```

### 3. 添加缓存层

对于频繁访问的用户信息，可以使用本地缓存：

```kotlin
@Cacheable("user-info", key = "#userId")
fun getUserInfo(userId: String): UserInfo {
    return userAuthClient.getUserInfo(userId)
}
```

## 总结

优化后的分布式认证系统具有以下特点：

1. **职责清晰**：用户模块负责认证，其他模块只验证
2. **性能优化**：下游服务本地验证 JWT，减少网络调用
3. **易于扩展**：新增模块只需依赖 shared-auth 即可
4. **安全可靠**：支持会话管理、令牌轮换、强制下线
5. **独立部署**：各模块可独立部署和扩展
