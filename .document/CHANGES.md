# 分布式认证系统优化 - 变更摘要

## 变更时间
2026-04-09

## 变更目的

优化项目的验证系统，使其更符合分布式微服务架构：
- 用户模块负责认证和授权
- 其他模块只验证 JWT 签名
- 各模块部署在不同机器上，拥有独立数据库

## 新增文件

### shared-auth 模块

1. `Wofuf-shared/Wofuf-auth/src/main/kotlin/dev/saraki/wofuf/auth/infra/JwtValidator.kt`
   - 统一的 JWT 验证器
   - 验证 JWT 签名和有效期
   - 提供解码功能（用于调试）

2. `Wofuf-shared/Wofuf-auth/src/main/kotlin/dev/saraki/wofuf/auth/infra/JwtAuthenticationFilter.kt`
   - 统一的 JWT 认证过滤器
   - 供所有下游微服务使用
   - 从 JWT Token 中提取用户信息并设置到 SecurityContext

3. `Wofuf-shared/Wofuf-auth/src/main/kotlin/dev/saraki/wofuf/auth/config/AuthFilterConfig.kt`
   - 注册 JwtAuthenticationFilter Bean

4. `Wofuf-shared/Wofuf-auth/build.gradle.kts`
   - 添加 `spring-boot-starter-web` 依赖

### 文档

1. `.document/distributed-auth-architecture.md`
   - 详细的架构文档
   - 包含组件说明、部署架构、配置说明等

2. `.document/quick-start-guide.md`
   - 快速启动指南
   - 包含测试流程和常见问题

3. `.document/CHANGES.md`（本文件）
   - 变更摘要

## 修改文件

### 论坛模块 (Wofuf-modules/Wofuf-forum)

1. **删除文件**
   - 删除 `src/main/kotlin/dev/saraki/wofuf/modules/forum/infra/auth/` 目录
   - 包含：`JwtUserAuthenticationTokenFilter.kt`, `ForumAuthenticationToken.kt`

2. **修改文件**
   - `src/main/kotlin/dev/saraki/wofuf/modules/forum/config/ForumSecurityConfig.kt`
     - 使用共享的 `JwtAuthenticationFilter`
     - 更新路由权限配置

   - `src/main/kotlin/dev/saraki/wofuf/modules/forum/infra/aop/PermissionAspect.kt`
     - 移除对 `ForumAuthenticationToken` 的引用
     - 改用 `JwtTokenInfo` 从 `authentication.details` 获取用户信息

   - `src/main/resources/application.yml`
     - 添加 Eureka 服务发现配置

### 网关 (Wofuf-infra/Wofuf-gateway)

1. `src/main/resources/application.yml`
   - 添加论坛路由配置

## 架构改进

### 之前的问题

1. **代码重复**：用户模块和论坛模块都有自己的 JWT 过滤器
2. **类型冲突**：两个模块都有 `JwtUserAuthenticationTokenFilter` 类
3. **依赖混乱**：论坛模块依赖用户模块的实现细节
4. **验证复杂**：论坛模块需要查询 Redis 验证会话

### 优化后的架构

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Gateway   │
└──────┬──────┘
       │
       ├──► User Module (Port 8001)
       │     ├─ JWT 签发
       │     ├─ 会话管理 (Redis)
       │     └─ 用户数据库
       │
       └──► Forum Module (Port 8003)
             ├─ JWT 验证 (本地)
             ├─ 权限检查 (本地 Member DB)
             └─ 论坛数据库
```

### 关键变化

1. **统一 JWT 验证**：所有下游服务使用共享的 `JwtValidator`
2. **本地验证**：下游服务只验证 JWT 签名，不查询 Redis
3. **权限分离**：管理员权限通过 JWT 标识，普通权限通过本地数据库
4. **独立部署**：各模块可独立部署和扩展

## 兼容性说明

### 向后兼容

- JWT Token 格式保持不变
- API 路径保持不变
- 客户端无需修改

### 配置要求

- 所有服务的 `auth.jwt.secret` 必须一致
- 论坛模块需要正确配置 Eureka
- 数据库中需要 Member 表（用于权限检查）

## 迁移指南

### 如果已经部署

1. 更新代码到最新版本
2. 确保 `shared-auth` 模块已构建
3. 重启所有服务（按启动顺序）
4. 验证 JWT Token 是否正常工作

### 数据库变更

无需数据库结构变更，但需要确保：

```sql
-- 检查 users 表
SELECT id, username, is_admin_user FROM users;

-- 检查 member 表（论坛模块）
SELECT member_id, user_id FROM member LIMIT 5;
```

## 测试清单

- [ ] 用户登录成功，获取 JWT Token
- [ ] 使用 Token 访问论坛接口成功
- [ ] 管理员 Token 可以访问管理员接口
- [ ] 普通用户 Token 被拒绝访问管理员接口
- [ ] Token 过期后无法访问受保护接口
- [ ] 无效 Token 被正确拒绝

## 已知问题

无

## 未来计划

1. **添加 OpenFeign 客户端**：下游服务可调用用户模块获取更多用户信息
2. **网关统一认证**：在网关层验证 JWT，减少下游服务负担
3. **添加缓存层**：缓存频繁访问的用户信息
4. **完善监控**：添加认证相关的监控和告警

## 联系方式

- 作者：YaeSaraki
- 邮箱：ikaraswork@iCloud.com
