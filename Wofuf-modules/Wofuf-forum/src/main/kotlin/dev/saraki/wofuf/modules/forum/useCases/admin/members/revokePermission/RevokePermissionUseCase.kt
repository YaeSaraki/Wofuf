package dev.saraki.wofuf.modules.forum.useCases.admin.members.revokePermission

import dev.saraki.wofuf.modules.forum.domain.OperationType
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.services.OperationLogService
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class RevokePermissionUseCase(
    private val memberRepo: MemberRepo,
    private val operationLogService: OperationLogService,
) : UseCase<RevokePermissionDto.Request, RevokePermissionDto.Response> {

    @RequirePermission(PermissionPoint.PERMISSION_GRANT, "Only users with PERMISSION_GRANT permission can revoke permissions")
    override fun execute(request: RevokePermissionDto.Request): Result<RevokePermissionDto.Response> {
        if (request.memberId.isBlank()) {
            return RevokePermissionErrors.MemberIdEmptyError()
        }

        if (request.operatorMemberId.isBlank()) {
            return RevokePermissionErrors.InvalidOperatorError()
        }

        val memberIdOrError = MemberId.create(UniqueEntityId(request.memberId))
        if (memberIdOrError.isFailure) {
            return RevokePermissionErrors.InvalidMemberIdError(request.memberId)
        }
        val memberId = memberIdOrError.getOrThrow()

        val operatorMemberIdOrError = MemberId.create(UniqueEntityId(request.operatorMemberId))
        if (operatorMemberIdOrError.isFailure) {
            return RevokePermissionErrors.InvalidOperatorError()
        }
        val operatorMemberId = operatorMemberIdOrError.getOrThrow()

        val member = memberRepo.findMemberById(memberId)
            ?: return RevokePermissionErrors.MemberNotFoundError(request.memberId)

        val revokeResult = member.revokePermission(request.permission)
        if (revokeResult.isFailure) {
            return RevokePermissionErrors.RevokeFailedError(request.memberId, request.permission.name, revokeResult.exceptionOrThrow().message ?: "Unknown error")
        }

        try {
            memberRepo.save(revokeResult.getOrThrow())

            // 记录操作日志
            operationLogService.logMemberAction(
                operationType = OperationType.MEMBER_REVOKE_PERMISSION,
                memberId = memberId,
                operatorId = operatorMemberId,
                details = "Revoked ${request.permission.name} from member"
            )
        } catch (e: Exception) {
            return RevokePermissionErrors.SaveFailedError(request.memberId)
        }

        return Result.success(RevokePermissionDto.Response(
            memberId = request.memberId,
            permission = request.permission.name,
            revoked = true
        ))
    }
}
