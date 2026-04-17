package dev.saraki.wofuf.modules.forum.useCases.admin.members.grantPermission

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class GrantPermissionErrors {
    class MemberIdEmptyError : Result.Failure<GrantPermissionDto.Response>(
        exception = UseCaseError(code = "MEMBER_ID_EMPTY_ERROR", message = "Member ID cannot be empty")
    )

    class InvalidMemberIdError(val memberId: String) : Result.Failure<GrantPermissionDto.Response>(
        exception = UseCaseError(code = "INVALID_MEMBER_ID_ERROR", message = "Invalid member ID format: $memberId")
    )

    class InvalidOperatorError : Result.Failure<GrantPermissionDto.Response>(
        exception = UseCaseError(code = "INVALID_OPERATOR_ERROR", message = "Invalid operator ID")
    )

    class MemberNotFoundError(val memberId: String) : Result.Failure<GrantPermissionDto.Response>(
        exception = UseCaseError(code = "MEMBER_NOT_FOUND_ERROR", message = "Couldn't find a member by memberId {$memberId}")
    )

    class GrantFailedError(val memberId: String, val permission: String, val reason: String) : Result.Failure<GrantPermissionDto.Response>(
        exception = UseCaseError(code = "GRANT_FAILED_ERROR", message = "Failed to grant permission '$permission' to member {$memberId}: $reason")
    )

    class SaveFailedError(val memberId: String) : Result.Failure<GrantPermissionDto.Response>(
        exception = UseCaseError(code = "SAVE_FAILED_ERROR", message = "Failed to save member {$memberId}")
    )
}
