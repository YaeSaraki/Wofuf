package dev.saraki.wofuf.modules.forum.useCases.members.getCurrentMember

import dev.saraki.wofuf.auth.infra.JwtAuthFilter
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15 16:15
 * @description Get current member information
 */
@Service
class GetCurrentMemberUseCase(
    private val memberRepo: MemberRepo,
) : UseCase<GetCurrentMemberDto.Request, GetCurrentMemberDto.Response> {
    override fun execute(request: GetCurrentMemberDto.Request): Result<GetCurrentMemberDto.Response> {
        if (request.userId.isBlank()) {
            return GetCurrentMemberErrors.UserIdEmptyError()
        }

        // Validate user ID
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return GetCurrentMemberErrors.MemberNotFoundError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()

        // Get member
        val member = memberRepo.findMemberByUserId(userId)
            ?: return GetCurrentMemberErrors.MemberNotFoundError(request.userId)

        // Check admin status from JWT (authoritative, set by users service during login)
        val isAdminUser = JwtAuthFilter.isAdmin()

        return Result.success(
            GetCurrentMemberDto.Response(
                memberId = member.memberId.stringValue,
                userId = member.userId.stringValue,
                playerId = member.playerId.stringValue,
                nickname = member.nickname.value,
                reputation = member.reputation,
                permissions = member.permissions.map { it.name },
                isAdminUser = isAdminUser,
                isBanned = member.isBanned,
                bannedAt = member.bannedAt?.atZone(java.time.ZoneId.systemDefault())?.toEpochSecond(),
                bannedUntil = member.bannedUntil?.atZone(java.time.ZoneId.systemDefault())?.toEpochSecond(),
                bannedReason = member.bannedReason,
            )
        )
    }
}
