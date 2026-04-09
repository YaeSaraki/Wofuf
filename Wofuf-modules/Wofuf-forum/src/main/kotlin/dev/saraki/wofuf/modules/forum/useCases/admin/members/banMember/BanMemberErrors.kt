package dev.saraki.wofuf.modules.forum.useCases.admin.members.banMember

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class BanMemberErrors {
    class MemberIdEmptyError : Result.Failure<BanMemberDto.Response>(
        exception = UseCaseError(code = "MEMBER_ID_EMPTY_ERROR", message = "Member ID cannot be empty")
    )

    class BannedByMemberIdEmptyError : Result.Failure<BanMemberDto.Response>(
        exception = UseCaseError(code = "BANNED_BY_MEMBER_ID_EMPTY_ERROR", message = "Banned by member ID cannot be empty")
    )

    class InvalidMemberIdError(val memberId: String) : Result.Failure<BanMemberDto.Response>(
        exception = UseCaseError(code = "INVALID_MEMBER_ID_ERROR", message = "Invalid member ID format: $memberId")
    )

    class MemberNotFoundError(val memberId: String) : Result.Failure<BanMemberDto.Response>(
        exception = UseCaseError(code = "MEMBER_NOT_FOUND_ERROR", message = "Couldn't find a member by memberId {$memberId}")
    )

    class BanFailedError(val memberId: String, val reason: String) : Result.Failure<BanMemberDto.Response>(
        exception = UseCaseError(code = "BAN_FAILED_ERROR", message = "Failed to ban member {$memberId}: $reason")
    )

    class SaveFailedError(val memberId: String) : Result.Failure<BanMemberDto.Response>(
        exception = UseCaseError(code = "SAVE_FAILED_ERROR", message = "Failed to save banned member {$memberId}")
    )
}
