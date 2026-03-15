package dev.saraki.wofuf.modules.forum.useCases.members.getMemberByUserName

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class GetMemberByUserNameErrors {

    // Username is empty
    class UsernameEmptyError() : Result.Failure<GetMemberByUserNameDto.Response>(
        exception = UseCaseError(
            code = "USERNAME_EMPTY",
            message = "Username cannot be empty"
        )
    )

    // Member not found
    class MemberNotFoundError(val username: String) : Result.Failure<GetMemberByUserNameDto.Response>(
        exception = UseCaseError(
            code = "MEMBER_NOT_FOUND",
            message = "Couldn't find a member by username {$username}"
        )
    )
}
