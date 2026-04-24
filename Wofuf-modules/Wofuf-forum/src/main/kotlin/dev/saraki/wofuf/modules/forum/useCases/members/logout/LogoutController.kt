package dev.saraki.wofuf.modules.forum.useCases.members.logout

import dev.saraki.wofuf.auth.infra.JwtUtils
import dev.saraki.wofuf.modules.forum.domain.services.MemberSessionDomainService
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.core.AppError
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.events.IDomainEvents
import org.springframework.web.bind.annotation.*

/**
 * 成员登出用例
 *
 * @author YaeSaraki
 * @date 2026/4/24
 */
@RestController("forumLogoutController")
@RequestMapping("/api/v1/forum/members")
class LogoutController(
    private val memberSessionDomainService: MemberSessionDomainService,
    private val memberRepo: MemberRepo,
    private val jwtUtils: JwtUtils,
    private val domainEvents: IDomainEvents
) {

    @PostMapping("/logout")
    fun logout(@RequestHeader("MeoKey") token: String): Result<LogoutResponse> {
        // 1. 解析 JWT 获取 userId 和 jti
        val claims = jwtUtils.parseClaims(token)
            ?: return Result.failure(AppError("无效的 Token"))

        // 注意：JWT 中 subject 是 username，uid 才是 userId
        val userIdStr = claims["uid"] as? String
            ?: return Result.failure(AppError("Token 中缺少用户信息"))

        val userId = UserId.create(UniqueEntityId(userIdStr)).getOrThrow()
        val jti = claims.id ?: return Result.failure(AppError("Token 中缺少会话标识"))

        // 2. 查找 Member
        val member = memberRepo.findMemberByUserId(userId)
            ?: return Result.failure(AppError("成员不存在"))

        // 3. 调用领域服务处理退出
        val logoutResult = memberSessionDomainService.handleLogout(member.memberId, jti)
        if (logoutResult.isFailure) {
            return logoutResult.map { LogoutResponse(true) }
        }

        // 4. 发布领域事件（发布到 Kafka）
        val updatedMember = logoutResult.getOrThrow()
        updatedMember.getDomainEvents().forEach { domainEvents.publish(it) }
        updatedMember.clearEvents()

        return Result.success(LogoutResponse(success = true))
    }
}

data class LogoutResponse(val success: Boolean)