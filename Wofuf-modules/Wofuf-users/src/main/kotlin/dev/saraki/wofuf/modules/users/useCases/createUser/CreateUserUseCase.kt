package dev.saraki.wofuf.modules.users.useCases.createUser

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/16 09:10
 *   @description:
 */

import dev.saraki.wofuf.modules.users.domain.User
import dev.saraki.wofuf.modules.users.domain.UserProps
import dev.saraki.wofuf.modules.users.domain.events.CreateUser
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserEmail
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserName
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserPassword
import dev.saraki.wofuf.modules.users.infra.repos.UserRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.events.IDomainEvents
import org.springframework.stereotype.Service

@Service
class CreateUserUseCase(
    private val userRepo: UserRepo,
    private val domainEvents: IDomainEvents,
) : UseCase<CreateUserDto.Request, User> {
    override fun execute(request: CreateUserDto.Request): Result<User> {
        val emailOrError = UserEmail.create(request.email)
        val usernameOrError = UserName.create(request.username)
        val passwordOrError = UserPassword.create(request.password, false)

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
            )
        )

        if (userResult.isFailure) {
            return Result.failure(userResult.exceptionOrThrow())
        }

        val user = userResult.getOrThrow()
        // 发布创建用户事件
        domainEvents.subscribe(CreateUserEventHandler())

        domainEvents.publishAll(user)

        userRepo.save(user)
        return Result.success(user)
    }
}