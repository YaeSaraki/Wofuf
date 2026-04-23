# 分布式认证系统优化 - 快速启动指南

## 优化内容

### 1. 新增组件

**shared-auth 模块**
- `JwtValidator` - 统一的 JWT 验证器
- `JwtAuthenticationFilter` - 统一的 JWT 认证过滤器
- `AuthFilterConfig` - 认证过滤器配置

### 2. 修改内容

**论坛模块 (Wofuf-modules/Wofuf-forum)**
- 移除了重复的认证代码
- 使用共享的 `JwtAuthenticationFilter`
- 更新 `PermissionAspect` 以适配新的认证机制
- 添加 Eureka 服务发现配置

**网关 (Wofuf-infra/Wofuf-gateway)**
- 添加论坛路由配置

### 3. 架构改进

- ✅ 统一 JWT 验证逻辑，消除代码重复
- ✅ 下游服务本地验证 JWT，减少网络调用
- ✅ 清晰的职责划分：用户模块负责认证，其他模块只验证
- ✅ 支持分布式部署，各模块独立运行

## 启动顺序

### 1. 启动基础设施

```bash
# 启动 Eureka 服务发现
./gradlew :infra-discovery:bootRun

# 启动 Redis（确保运行在 localhost:6380）
# 启动 MySQL（确保运行在 localhost:3307）
```

### 2. 启动网关

```bash
./gradlew :infra-gateway:bootRun
```

### 3. 启动用户模块

```bash
./gradlew :modules-users:bootRun
```

### 4. 启动论坛模块

```bash
./gradlew :modules-forum:bootRun
```

## 测试流程

### 1. 用户登录

```bash
curl -X POST http://localhost:9999/api/v1/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "YaeSaraki",
    "password": "your-password"
  }'
```

响应示例：
```json
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

### 2. 设置用户为管理员（如果需要）

```bash
mysql -h localhost -P 3307 -u Woffo_db_user -ppassword Woffo_db

UPDATE users SET is_admin_user = TRUE WHERE username = 'YaeSaraki';
```

### 3. 调用论坛接口

```bash
# 创建帖子
curl -X POST http://localhost:9999/api/v1/forum/posts \
  -H "Content-Type: application/json" \
  -H "MeoKey: your-access-token" \
  -d '{
    "title": "测试帖子",
    "content": "这是测试内容",
    "categoryId": "default"
  }'
```

### 4. 调用管理员接口

```bash
# 设置帖子为审核中
curl -X POST http://localhost:9999/api/v1/forum/admin/posts/{postId}/review \
  -H "MeoKey: your-access-token"
```

## 常见问题

### Q1: 编译失败

```bash
# 清理并重新编译
./gradlew clean build
```

### Q2: JWT 验证失败

**检查项：**
1. 所有服务的 `auth.jwt.secret` 配置是否一致
2. Token 是否在有效期内
3. Token 格式是否正确（Bearer 前缀可选）

### Q3: 服务发现失败

**检查项：**
1. Eureka 服务是否正常运行
2. 服务的 Eureka 配置是否正确
3. 网络是否可达

### Q4: 权限验证失败

**检查项：**
1. 用户是否已登录（Token 是否有效）
2. 用户的 `is_admin_user` 是否为 TRUE（针对管理员接口）
3. 本地数据库中是否有对应的 Member 记录

## 配置检查清单

### 用户模块 (modules-users)

```yaml
server:
  port: 8001

auth:
  jwt:
    secret: your-secret-key-must-be-at-least-32-bytes-long-and-secure!
    expiration: 3600000
    refresh-expiration: 604800000

spring:
  application:
    name: modules-users
  datasource:
    url: jdbc:mysql://localhost:3307/Woffo_db
  data:
    redis:
      host: localhost
      port: 6380

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

### 论坛模块 (modules-forum)

```yaml
server:
  port: 8003

auth:
  jwt:
    secret: your-secret-key-must-be-at-least-32-bytes-long-and-secure!
    expiration: 3600000
    refresh-expiration: 604800000

spring:
  application:
    name: modules-forum
  datasource:
    url: jdbc:mysql://localhost:3307/Woffo_db

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

### 网关 (infra-gateway)

```yaml
server:
  port: 9999

spring:
  application:
    name: infra-gateway
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

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

## 调试技巧

### 1. 启用调试日志

```yaml
logging:
  level:
    dev.saraki.wofuf.auth: DEBUG
    dev.saraki.wofuf.modules.forum: DEBUG
    dev.saraki.wofuf.modules.users: DEBUG
```

### 2. 检查 Eureka 注册情况

访问：http://localhost:8761

查看已注册的服务列表。

### 3. 检查 JWT Token 内容

```kotlin
// 使用 JwtValidator.decodeToken() 方法
val tokenInfo = jwtValidator.decodeToken(token)
println(tokenInfo)
```

## 下一步

1. **添加更多模块**：参考论坛模块的认证实现，为其他模块添加 JWT 验证
2. **完善权限系统**：在论坛模块中实现细粒度的权限点控制
3. **添加缓存**：对频繁访问的用户信息添加缓存层
4. **监控和日志**：添加认证相关的监控和日志收集

## 联系方式

如有问题，请联系：
- 作者：YaeSaraki
- 邮箱：ikaraswork@iCloud.com
