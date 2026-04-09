package dev.saraki.wofuf.modules.forum.useCases.admin.members.banMember

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BanMemberUseCase(
    private val memberRepo: MemberRepo,
) : UseCase<BanMemberDto.Request, BanMemberDto.Response> {

    @RequirePermission(PermissionPoint.USER_BAN, "Only users with USER_BAN permission can ban members")
    override fun execute(request: BanMemberDto.Request): Result<BanMemberDto.Response> {
        if (request.memberId.isBlank()) {
            return BanMemberErrors.MemberIdEmptyError()
        }

        if (request.bannedByMemberId.isBlank()) {
            return BanMemberErrors.BannedByMemberIdEmptyError()
        }

        val memberIdOrError = MemberId.create(UniqueEntityId(request.memberId))
        if (memberIdOrError.isFailure) {
            return BanMemberErrors.InvalidMemberIdError(request.memberId)
        }
        val memberId = memberIdOrError.getOrThrow()

        val bannedByMemberIdOrError = MemberId.create(UniqueEntityId(request.bannedByMemberId))
        if (bannedByMemberIdOrError.isFailure) {
            return BanMemberErrors.InvalidMemberIdError(request.bannedByMemberId)
        }
        val bannedByMemberId = bannedByMemberIdOrError.getOrThrow()

        val member = memberRepo.findMemberById(memberId)
            ?: return BanMemberErrors.MemberNotFoundError(request.memberId)

        val bannedUntil = request.bannedUntilMinutes?.let { minutes ->
            LocalDateTime.now().plusMinutes(minutes.toLong())
        } ?: LocalDateTime.now().plusYears(100) // Default to 100 years if not specified (permanent ban)

        val banResult = member.ban(
            until = bannedUntil,
            reason = request.reason ?: "",
            by = bannedByMemberId
        )
        if (banResult.isFailure) {
            return BanMemberErrors.BanFailedError(request.memberId, banResult.exceptionOrThrow().message ?: "Unknown error")
        }

        try {
            memberRepo.save(banResult.getOrThrow())
        } catch (e: Exception) {
            return BanMemberErrors.SaveFailedError(request.memberId)
        }

        return Result.success(BanMemberDto.Response(
            memberId = request.memberId,
            isBanned = true,
            bannedUntil = bannedUntil?.toString()
        ))
    }
}
