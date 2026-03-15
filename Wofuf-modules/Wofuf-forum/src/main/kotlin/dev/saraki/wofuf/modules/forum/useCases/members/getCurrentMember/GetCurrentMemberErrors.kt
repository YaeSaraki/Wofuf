package dev.saraki.wofuf.modules.forum.useCases.members.getCurrentMember

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class GetCurrentMemberErrors {

    // User ID is empty
    class UserIdEmptyError() : Result.Failure<GetCurrentMemberDto.Response>(
        exception = UseCaseError(
            code = "USER_ID_EMPTY",
            message = "User ID cannot be empty"
        )
    )

    // Member not found
    class MemberNotFoundError(val userId: String) : Result.Failure<GetCurrentMemberDto.Response>(
        exception = UseCaseError(
            code = "MEMBER_NOT_FOUND",
            message = "Couldn't find a member by userId {$userId}"
        )
    )
}
