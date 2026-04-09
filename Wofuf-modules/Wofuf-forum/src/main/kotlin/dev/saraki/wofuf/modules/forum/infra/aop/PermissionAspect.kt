package dev.saraki.wofuf.modules.forum.infra.aop

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.exception.PermissionDeniedException
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * 论坛权限验证切面
 * 拦截带有 @RequirePermission 注解的方法，验证用户权限
 *
 * 权限控制分层：
 * 1. 系统级权限：isAdmin - 由 JWT 验证，由 JwtAuthFilter 设置
 * 2. 论坛级权限：论坛权限点 - 由论坛模块自己管理，查询本地数据库
 *
 * @author YaeSaraki
 */
@Aspect
@Component
class PermissionAspect {

    private val log = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var memberRepo: MemberRepo

    @Around("@annotation(requirePermission)")
    fun checkPermission(joinPoint: ProceedingJoinPoint, requirePermission: RequirePermission): Any {
        // 1. 检查是否已登录
        if (!dev.saraki.wofuf.auth.infra.JwtAuthFilter.isAuthenticated()) {
            log.warn("Permission check failed: user not authenticated")
            throw PermissionDeniedException("用户未登录")
        }

        // 2. 获取当前用户 ID
        val userId = dev.saraki.wofuf.auth.infra.JwtAuthFilter.getCurrentUserId()
        if (userId == null) {
            log.warn("Permission check failed: userId not found")
            throw PermissionDeniedException("无效的认证信息")
        }

        // 3. 检查是否为系统管理员
        if (dev.saraki.wofuf.auth.infra.JwtAuthFilter.isAdmin()) {
            log.debug("User {} is system admin, permission granted", userId)
            return joinPoint.proceed()
        }

        // 4. 查找论坛 Member 并检查权限
        val userIdObj = UserId.create(UniqueEntityId(userId)).getOrThrow()
        val member = memberRepo.findMemberByUserId(userIdObj)
        if (member == null) {
            log.warn("Permission check failed: member not found for user {}", userId)
            throw PermissionDeniedException("用户信息不存在")
        }

        // 5. 检查是否有对应权限点
        if (!member.hasPermission(requirePermission.permission)) {
            log.warn(
                "Permission denied for user {}: required {}, but user has {}",
                userId,
                requirePermission.permission.name,
                member.permissions.map { it.name }
            )
            throw PermissionDeniedException(requirePermission.message)
        }

        log.debug("Permission granted for user {}: {}", userId, requirePermission.permission.name)
        return joinPoint.proceed()
    }
}
