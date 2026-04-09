package dev.saraki.wofuf.modules.users.useCases.getCurrentUser

import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.modules.users.infra.repos.UserRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class GetCurrentUserUseCase(
    private val userRepo: UserRepo
) {
    fun execute(request: GetCurrentUserDto.Request): Result<GetCurrentUserDto.Response> {
        // 验证 userId
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return Result.failure(userIdOrError.exceptionOrThrow())
        }
        val userId = userIdOrError.getOrThrow()

        // 查找用户
        val user = userRepo.findUserByUserId(userId)
            ?: return GetCurrentUserErrors.UserNotFoundError(request.userId)

        return Result.success(
            GetCurrentUserDto.Response(
                userId = user.userId.stringValue,
                username = user.username.value,
                email = user.email.value,
                isEmailVerified = user.isEmailVerified,
                isAdminUser = user.isAdminUser,
                lastLogin = user.lastLogin,
            )
        )
    }
}
