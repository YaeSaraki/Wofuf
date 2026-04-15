package dev.saraki.wofuf.modules.forum.useCases.admin.members.getMemberProfile

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class GetMemberProfileErrors {
    class MemberIdEmptyError : Result.Failure<GetMemberProfileDto.Response>(
        exception = UseCaseError(code = "MEMBER_ID_EMPTY", message = "Member ID cannot be empty")
    )

    class InvalidMemberIdError(val memberId: String) : Result.Failure<GetMemberProfileDto.Response>(
        exception = UseCaseError(code = "INVALID_MEMBER_ID", message = "Invalid member ID: $memberId")
    )

    class MemberNotFoundError(val memberId: String) : Result.Failure<GetMemberProfileDto.Response>(
        exception = UseCaseError(code = "MEMBER_NOT_FOUND", message = "Member not found: $memberId")
    )
}
