package dev.saraki.wofuf.modules.users.useCases.login

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 11:07
 *   @description:
 */

import dev.saraki.wofuf.modules.users.domain.UserName
import dev.saraki.wofuf.modules.users.domain.UserPassword
import dev.saraki.wofuf.modules.users.infra.repos.UserRepo
import dev.saraki.wofuf.modules.users.services.auth.IAuth
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service

@Service
class LoginUseCase(
    val userRepo: UserRepo,
    val authService: IAuth
) : UseCase<LoginDto.LoginRequest, LoginDto.LoginResponse> {
    override fun execute(request: LoginDto.LoginRequest): Result<LoginDto.LoginResponse> {
        // 验证用户名和密码
        val userNameOrError = UserName.create(request.username)
        val passwordOrError = UserPassword.create(request.password)
        val payloadResult = Result.combine(userNameOrError, passwordOrError)
        if (payloadResult.isFailure) {
            return Result.failure(payloadResult.exceptionOrThrow())
        }

        val userName = userNameOrError.getOrThrow()
        val password = passwordOrError.getOrThrow()

        // 查找用户实体
        val user = userRepo.findUserByUserName(userName)
        if (user == null) {
            return LoginErrors.UserNotFoundError(userName.value)
        }

        // 检查密码是否匹配
        val passwordValid = user.password.matches(password.getHashedValue())
        if (!passwordValid) {
            return LoginErrors.PasswordNotMatchError(userName.value)
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