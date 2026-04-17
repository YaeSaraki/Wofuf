package dev.saraki.wofuf.modules.forum.useCases.admin.members.revokePermission

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class RevokePermissionErrors {
    class MemberIdEmptyError : Result.Failure<RevokePermissionDto.Response>(
        exception = UseCaseError(code = "MEMBER_ID_EMPTY_ERROR", message = "Member ID cannot be empty")
    )

    class InvalidMemberIdError(val memberId: String) : Result.Failure<RevokePermissionDto.Response>(
        exception = UseCaseError(code = "INVALID_MEMBER_ID_ERROR", message = "Invalid member ID format: $memberId")
    )

    class InvalidOperatorError : Result.Failure<RevokePermissionDto.Response>(
        exception = UseCaseError(code = "INVALID_OPERATOR_ERROR", message = "Invalid operator ID")
    )

    class MemberNotFoundError(val memberId: String) : Result.Failure<RevokePermissionDto.Response>(
        exception = UseCaseError(code = "MEMBER_NOT_FOUND_ERROR", message = "Couldn't find a member by memberId {$memberId}")
    )

    class RevokeFailedError(val memberId: String, val permission: String, val reason: String) : Result.Failure<RevokePermissionDto.Response>(
        exception = UseCaseError(code = "REVOKE_FAILED_ERROR", message = "Failed to revoke permission '$permission' from member {$memberId}: $reason")
    )

    class SaveFailedError(val memberId: String) : Result.Failure<RevokePermissionDto.Response>(
        exception = UseCaseError(code = "SAVE_FAILED_ERROR", message = "Failed to save member {$memberId}")
    )
}
