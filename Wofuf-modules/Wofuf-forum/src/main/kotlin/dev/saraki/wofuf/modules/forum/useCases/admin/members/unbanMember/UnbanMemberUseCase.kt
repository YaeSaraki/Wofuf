package dev.saraki.wofuf.modules.forum.useCases.admin.members.unbanMember

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class UnbanMemberUseCase(
    private val memberRepo: MemberRepo,
) : UseCase<UnbanMemberDto.Request, UnbanMemberDto.Response> {

    @RequirePermission(PermissionPoint.USER_BAN, "Only users with USER_BAN permission can unban members")
    override fun execute(request: UnbanMemberDto.Request): Result<UnbanMemberDto.Response> {
        if (request.memberId.isBlank()) {
            return UnbanMemberErrors.MemberIdEmptyError()
        }

        val memberIdOrError = MemberId.create(UniqueEntityId(request.memberId))
        if (memberIdOrError.isFailure) {
            return UnbanMemberErrors.InvalidMemberIdError(request.memberId)
        }
        val memberId = memberIdOrError.getOrThrow()

        val member = memberRepo.findMemberById(memberId)
            ?: return UnbanMemberErrors.MemberNotFoundError(request.memberId)

        val unbanResult = member.unban()
        if (unbanResult.isFailure) {
            return UnbanMemberErrors.UnbanFailedError(request.memberId, unbanResult.exceptionOrThrow().message ?: "Unknown error")
        }

        try {
            memberRepo.save(unbanResult.getOrThrow())
        } catch (e: Exception) {
            return UnbanMemberErrors.SaveFailedError(request.memberId)
        }

        return Result.success(UnbanMemberDto.Response(memberId = request.memberId, isBanned = false))
    }
}
