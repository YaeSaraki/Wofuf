package dev.saraki.wofuf.modules.forum.useCases.admin.members.grantPermission

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class GrantPermissionUseCase(
    private val memberRepo: MemberRepo,
) : UseCase<GrantPermissionDto.Request, GrantPermissionDto.Response> {

    @RequirePermission(PermissionPoint.PERMISSION_GRANT, "Only users with PERMISSION_GRANT permission can grant permissions")
    override fun execute(request: GrantPermissionDto.Request): Result<GrantPermissionDto.Response> {
        if (request.memberId.isBlank()) {
            return GrantPermissionErrors.MemberIdEmptyError()
        }

        val memberIdOrError = MemberId.create(UniqueEntityId(request.memberId))
        if (memberIdOrError.isFailure) {
            return GrantPermissionErrors.InvalidMemberIdError(request.memberId)
        }
        val memberId = memberIdOrError.getOrThrow()

        val member = memberRepo.findMemberById(memberId)
            ?: return GrantPermissionErrors.MemberNotFoundError(request.memberId)

        val grantResult = member.grantPermission(request.permission)
        if (grantResult.isFailure) {
            return GrantPermissionErrors.GrantFailedError(request.memberId, request.permission.name, grantResult.exceptionOrThrow().message ?: "Unknown error")
        }

        try {
            memberRepo.save(grantResult.getOrThrow())
        } catch (e: Exception) {
            return GrantPermissionErrors.SaveFailedError(request.memberId)
        }

        return Result.success(GrantPermissionDto.Response(
            memberId = request.memberId,
            permission = request.permission.name,
            granted = true
        ))
    }
}
