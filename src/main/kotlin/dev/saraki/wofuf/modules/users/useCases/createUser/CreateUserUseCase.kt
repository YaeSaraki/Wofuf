package dev.saraki.wofuf.modules.users.useCases.createUser

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/16 09:10
 *   @description:
 */

import dev.saraki.wofuf.modules.users.domain.*
import dev.saraki.wofuf.modules.users.infra.repos.UserRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class CreateUserUseCase(
    private val userRepo: UserRepo
) : UseCase<CreateUserDto, User> {
    override fun execute(request: CreateUserDto): Result<User> {
        val emailOrError = UserEmail.create(request.email)
        val usernameOrError = UserName.create(request.username)
        val passwordOrError = UserPassword.create(request.password)

        val dtoResult = Result.combine(emailOrError, usernameOrError, passwordOrError)

        if (dtoResult.isFailure) {
            return Result.failure(dtoResult.exceptionOrThrow())
        }

        // 检查邮箱和用户名是否已存在
        val email = emailOrError.getOrThrow()
        val username = usernameOrError.getOrThrow()
        val password = passwordOrError.getOrThrow()

        // 检查邮箱是否已存在
        val userAlreadyExists = userRepo.findByUserEmail(email)
        if (userAlreadyExists != null) {
            return CreateUserErrors.EmailAlreadyExistsError(email.value)
        }

        // 检查用户名是否已存在
        val userByName = userRepo.existsByUserName(username)
        if (userByName) {
            return CreateUserErrors.UsernameAlreadyExistsError(username.value)
        }

        val userResult = User.create(
            props = UserProps(
                email = email,
                name = username,
                password = password,
                isEmailVerified = false,
                isAdminUser = false,
                accessToken = null,
                isDeleted = false,
                lastLogin = null,
            ),
            id = UniqueEntityId()
        )
        if (userResult.isFailure) {
            return Result.failure(userResult.exceptionOrThrow())
        }

        val user = userResult.getOrThrow()

        userRepo.save(user)
        return Result.success(user)
    }
}