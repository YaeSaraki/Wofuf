package dev.saraki.wofuf.modules.users.useCases.login

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 11:07
 *   @description:
 */

import dev.saraki.wofuf.modules.users.domain.UserName
import dev.saraki.wofuf.modules.users.domain.UserPassword
import dev.saraki.wofuf.modules.users.mappers.UserMap
import dev.saraki.wofuf.modules.users.infra.repos.IUserRepo
import dev.saraki.wofuf.modules.users.services.auth.IAuth
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service

@Service
class LoginUseCase(val userRepo: IUserRepo, val authService: IAuth): UseCase<LoginDto.LoginRequest, LoginDto.LoginResponse> {
    override fun execute(request: LoginDto.LoginRequest): Result<LoginDto.LoginResponse> {
        // 验证用户名和密码
        val usernameOrError = UserName.create(request.username)
        val passwordOrError = UserPassword.create(request.password)
        val payloadResult = Result.combine(usernameOrError, passwordOrError)
        if (payloadResult.isFailure) {
            return Result.failure(payloadResult.exceptionOrThrow())
        }
        val username = usernameOrError.getOrThrow()
        val password = passwordOrError.getOrThrow()

        // 查找用户实体
        val userEntityOrNull = userRepo.findUserByUsername(username.value).orElse(null)
        if (userEntityOrNull == null) {
            return LoginErrors.UserNotFoundError(username.value)
        }
        val userEntity = userEntityOrNull
        val user = UserMap.from(userEntity).toDomain()

        // 检查密码是否匹配
        val passwordValid = user.userProps.password.matches(password.getHashedValue())
        if (!passwordValid) {
            return LoginErrors.PasswordNotMatchError(username.value)
        }

        val authSession = authService.login(user)
        user.setAccessToken(authSession.accessToken, authSession.refreshToken)

        // 返回登录响应
        val loginResponseDto = LoginDto.LoginResponse(
            accessToken = authSession.accessToken,
            refreshToken = authSession.refreshToken,
        )
        return Result.success(loginResponseDto)
    }
}