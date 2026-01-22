package dev.saraki.wofuf.modules.users.useCases.getUserByUsername

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/17 21:52
 *   @description:
 */

import dev.saraki.wofuf.modules.users.domain.UserName
import dev.saraki.wofuf.modules.users.mappers.UserMap
import dev.saraki.wofuf.modules.users.infra.repos.IUserRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class GetUserByUsernameUseCase(private val userRepo: IUserRepo): UseCase<GetUserByUsernameDto.GetUserRequest, GetUserByUsernameDto.GetUserResponse> {
    override fun execute(request: GetUserByUsernameDto.GetUserRequest): Result<GetUserByUsernameDto.GetUserResponse> {
        // 检测用户名是否有效
        val userNameOrError = UserName.create(request.username)
        if (userNameOrError.isFailure) {
            return Result.failure(userNameOrError.exceptionOrThrow())
        }

        // 检测用户是否存在
        val userEntity = userRepo.findUserByUsername(request.username)
        if (!userEntity.isPresent) {
            return GetUserByUsernameErrors.UserNotFoundError(request.username)
        }

        // 映射用户实体到用户 DTO
        val userDto = UserMap.from(userEntity.get())
        val userByUsernameDto = GetUserByUsernameDto.GetUserResponse(
            username = userDto.userName,
            email = userDto.email,
            isEmailVerified = userDto.isEmailVerified,
            adminUser = userDto.isAdminUser,
            lastLogin = userDto.lastLogin ?: LocalDateTime.MIN,
        )
        return Result.success(userByUsernameDto)
    }
}