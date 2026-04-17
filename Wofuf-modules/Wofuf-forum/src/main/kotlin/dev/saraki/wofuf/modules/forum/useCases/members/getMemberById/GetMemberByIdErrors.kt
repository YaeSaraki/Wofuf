package dev.saraki.wofuf.modules.forum.useCases.members.getMemberById

import dev.saraki.wofuf.modules.forum.useCases.members.getMemberById.GetMemberByIdDto.Response
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 * @author YaeSaraki
 * @date 2026/4/16
 * @description Error classes for get member by ID use case
 */
class GetMemberByIdErrors {

    /** Member ID is empty */
    class MemberIdEmptyError : Result.Failure<Response>(
        exception = UseCaseError(
            code = "MEMBER_ID_EMPTY_ERROR",
            message = "Member ID cannot be empty"
        )
    )

    /** Invalid member ID format */
    class InvalidMemberIdError(val memberId: String) : Result.Failure<Response>(
        exception = UseCaseError(
            code = "INVALID_MEMBER_ID_ERROR",
            message = "Invalid member ID format: $memberId"
        )
    )

    /** Member not found */
    class MemberNotFoundError(val memberId: String) : Result.Failure<Response>(
        exception = UseCaseError(
            code = "MEMBER_NOT_FOUND_ERROR",
            message = "Couldn't find a member by ID {$memberId}"
        )
    )
}