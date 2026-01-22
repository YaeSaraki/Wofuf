package dev.saraki.wofuf.modules.users.useCases.createUser

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/16 09:10
 *   @description:
 */

import dev.saraki.wofuf.modules.users.domain.*
import dev.saraki.wofuf.modules.users.dtos.UserDto
import dev.saraki.wofuf.modules.users.mappers.UserMap
import dev.saraki.wofuf.modules.users.infra.repos.IUserRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service

@Service
class CreateUserUseCase(private val userRepo: IUserRepo) : UseCase<CreateUserDto, User> {
    override fun execute(request: CreateUserDto): Result<User> {
        val idOrError = UserId.create()
        val emailOrError = UserEmail.create(request.email)
        val usernameOrError = UserName.create(request.username)
        val passwordOrError = UserPassword.create(request.password)

        val dtoResult = Result.combine(idOrError, emailOrError, usernameOrError, passwordOrError)

        if (dtoResult.isFailure) {
            return Result.failure(dtoResult.exceptionOrThrow())
        }

        // 检查邮箱和用户名是否已存在
        val id = idOrError.getOrThrow()
        val email = emailOrError.getOrThrow()
        val username = usernameOrError.getOrThrow()
        val password = passwordOrError.getOrThrow()

        // 检查id是否已存在
        val userById = userRepo.findById(id.value.id).isPresent
        if (userById) {
            return CreateUserErrors.IdAlreadyExistsError(id.value.id)
        }

        // 检查邮箱是否已存在
        val userAlreadyExists = userRepo.findByEmail(email.value).isPresent
        if (userAlreadyExists) {
            return CreateUserErrors.EmailAlreadyExistsError(email.value)
        }

        // 检查用户名是否已存在
        val userByName = userRepo.existsByUsername(username.value)
        if (userByName) {
            return CreateUserErrors.UsernameAlreadyExistsError(username.value)
        }

        val userResult = User.create(
            props = UserProps(
                email = email,
                username = username,
                password = password,
                isEmailVerified = false,
                isAdminUser = false,
                accessToken = null,
                isDeleted = false,
                lastLogin = null,
            ),
            id
        )
        if (userResult.isFailure) {
            return Result.failure(userResult.exceptionOrThrow())
        }

        // 保存用户
        val userEntity = UserMap.from(userResult.getOrThrow()).toEntity()
        userRepo.save(userEntity)

        return Result.success(userResult.getOrThrow())
    }
}