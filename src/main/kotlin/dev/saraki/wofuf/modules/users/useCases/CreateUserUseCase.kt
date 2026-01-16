package dev.saraki.wofuf.modules.users.useCases

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/16 09:10
 *   @description:
 */
import dev.saraki.wofuf.modules.users.domain.User
import dev.saraki.wofuf.modules.users.domain.UserEmail
import dev.saraki.wofuf.modules.users.domain.UserName
import dev.saraki.wofuf.modules.users.domain.UserPassword
import dev.saraki.wofuf.modules.users.dtos.CreateUserRequest
import dev.saraki.wofuf.modules.users.dtos.UserDto
import dev.saraki.wofuf.modules.users.repos.UserRepo
import dev.saraki.wofuf.shared.core.UseCase

class CreateUserUseCase(private val userRepo: UserRepo) : UseCase<CreateUserRequest, UserDto>() {
    override fun execute(request: CreateUserRequest): Result<UserDto> {
        // 创建值对象
        val emailResult = UserEmail.create(request.email)
        if (emailResult.isFailure) {
            return Result.failure(Exception(emailResult.exceptionOrNull()))
        }

        val usernameResult = UserName.create(request.username)
        if (usernameResult.isFailure) {
            return Result.failure(Exception(usernameResult.exceptionOrNull()))
        }

        val passwordResult = UserPassword.create(request.password)
        if (passwordResult.isFailure) {
            return Result.failure(Exception(passwordResult.exceptionOrNull()))
        }

        // 检查邮箱和用户名是否已存在
        if (userRepo.findByEmail(emailResult.getOrNull().toString()).isPresent) {
            return Result.failure(Exception("Email already exists"))
        }

        if (userRepo.findByUserName(usernameResult.getOrNull().toString()).isPresent) {
            return Result.failure(Exception("Username already exists"))
        }

        // 创建用户
        val userResult = User.create(
            email = emailResult.getOrThrow(),
            username = usernameResult.getOrThrow(),
            password = passwordResult.getOrThrow()
        )

        if (userResult.isFailure) {
            return Result.failure(Exception(userResult.exceptionOrNull()))
        }

        // 保存用户
        val savedUser = userRepo.save(userResult.getOrThrow())

        // 返回DTO
        return Result.success(UserDto.fromDomain(savedUser))
    }
}

