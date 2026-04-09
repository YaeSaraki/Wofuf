package dev.saraki.wofuf.modules.users.useCases.getCurrentUser

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class GetCurrentUserErrors {
    class UserNotFoundError(val userId: String) : Result.Failure<GetCurrentUserDto.Response>(
        exception = UseCaseError(
            code = "USER_NOT_FOUND",
            message = "The user cannot be found: $userId"
        )
    )
}
