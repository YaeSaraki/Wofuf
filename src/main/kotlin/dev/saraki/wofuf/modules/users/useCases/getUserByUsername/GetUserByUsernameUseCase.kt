package dev.saraki.wofuf.modules.users.useCases.getUserByUsername

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/17 21:52
 *   @description:
 */

import dev.saraki.wofuf.modules.users.domain.UserName
import dev.saraki.wofuf.modules.users.infra.repos.UserRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class GetUserByUsernameUseCase(
    private val userRepo: UserRepo
) : UseCase<GetUserByUsernameDto.GetUserRequest, GetUserByUsernameDto.GetUserResponse> {
    override fun execute(request: GetUserByUsernameDto.GetUserRequest): Result<GetUserByUsernameDto.GetUserResponse> {
        // 检测用户名是否有效
        val userNameOrError = UserName.create(request.username)
        if (userNameOrError.isFailure) {
            return Result.failure(userNameOrError.exceptionOrThrow())
        }
        val userName = userNameOrError.getOrThrow()

        // 检测用户是否存在
        val user = userRepo.findUserByUserName(userName)
        if (user == null) {
            return GetUserByUsernameErrors.UserNotFoundError(request.username)
        }

        val userByUsernameDto = GetUserByUsernameDto.GetUserResponse(
            username = user.username.value,
            email = user.email.value,
            isEmailVerified = user.isEmailVerified,
            adminUser = user.isAdminUser,
            lastLogin = user.lastLogin ?: LocalDateTime.MIN,
        )

        return Result.success(userByUsernameDto)
    }
}