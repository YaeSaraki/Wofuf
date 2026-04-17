package dev.saraki.wofuf.modules.forum.useCases.members.updateNickname

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class UpdateMemberNicknameErrors {
    class MemberIdEmptyError : Result.Failure<UpdateMemberNicknameDto.Response>(
        exception = UseCaseError(code = "MEMBER_ID_EMPTY", message = "Member ID cannot be empty")
    )

    class NicknameEmptyError : Result.Failure<UpdateMemberNicknameDto.Response>(
        exception = UseCaseError(code = "NICKNAME_EMPTY", message = "Nickname cannot be empty")
    )

    class InvalidNicknameError : Result.Failure<UpdateMemberNicknameDto.Response>(
        exception = UseCaseError(code = "INVALID_NICKNAME", message = "Invalid nickname format")
    )

    class MemberNotFoundError : Result.Failure<UpdateMemberNicknameDto.Response>(
        exception = UseCaseError(code = "MEMBER_NOT_FOUND", message = "Member not found")
    )

    class NicknameAlreadyTakenError : Result.Failure<UpdateMemberNicknameDto.Response>(
        exception = UseCaseError(code = "NICKNAME_ALREADY_TAKEN", message = "Nickname is already taken")
    )

    class ForbiddenError : Result.Failure<UpdateMemberNicknameDto.Response>(
        exception = UseCaseError(code = "FORBIDDEN", message = "You can only update your own nickname")
    )
}
