package dev.saraki.wofuf.modules.forum.useCases.admin.members.unbanMember

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class UnbanMemberErrors {
    class MemberIdEmptyError : Result.Failure<UnbanMemberDto.Response>(
        exception = UseCaseError(code = "MEMBER_ID_EMPTY_ERROR", message = "Member ID cannot be empty")
    )

    class InvalidMemberIdError(val memberId: String) : Result.Failure<UnbanMemberDto.Response>(
        exception = UseCaseError(code = "INVALID_MEMBER_ID_ERROR", message = "Invalid member ID format: $memberId")
    )

    class MemberNotFoundError(val memberId: String) : Result.Failure<UnbanMemberDto.Response>(
        exception = UseCaseError(code = "MEMBER_NOT_FOUND_ERROR", message = "Couldn't find a member by memberId {$memberId}")
    )

    class UnbanFailedError(val memberId: String, val reason: String) : Result.Failure<UnbanMemberDto.Response>(
        exception = UseCaseError(code = "UNBAN_FAILED_ERROR", message = "Failed to unban member {$memberId}: $reason")
    )

    class SaveFailedError(val memberId: String) : Result.Failure<UnbanMemberDto.Response>(
        exception = UseCaseError(code = "SAVE_FAILED_ERROR", message = "Failed to save unbanned member {$memberId}")
    )
}
