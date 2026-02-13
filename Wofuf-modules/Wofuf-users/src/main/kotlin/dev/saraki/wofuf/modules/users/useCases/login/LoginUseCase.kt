package dev.saraki.wofuf.modules.users.useCases.login

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 11:07
 *   @description:
 */

import dev.saraki.wofuf.modules.users.domain.valueObjects.UserName
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserPassword
import dev.saraki.wofuf.modules.users.infra.repos.UserRepo
import dev.saraki.wofuf.modules.users.services.auth.UserAuthService
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.events.IDomainEvents
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service

@Service
class LoginUseCase(
    val userRepo: UserRepo,
    val domainEvents: IDomainEvents,
    val userAuthService: UserAuthService,
    val authenticationManager: AuthenticationManager
) : UseCase<LoginDto.Request, LoginDto.Response> {
    override fun execute(request: LoginDto.Request): Result<LoginDto.Response> {
        // 验证用户名和密码
        val userNameOrError = UserName.create(request.username)
        val passwordOrError = UserPassword.create(request.password, false)
        val payloadResult = Result.combine(userNameOrError, passwordOrError)
        if (payloadResult.isFailure) {
            return Result.failure(payloadResult.exceptionOrThrow())
        }

        val userName = userNameOrError.getOrThrow()

        // 查找用户实体
        val user = userRepo.findUserByUserName(userName) ?: return LoginErrors.UserNotFoundError(userName.value)

        val authenticationToken = UsernamePasswordAuthenticationToken(request.username, request.password)
        val authenticate =  authenticationManager.authenticate(authenticationToken)
        if (!authenticate.isAuthenticated) {
            return LoginErrors.AuthenticationFailedError(userName.value)
        }

        val authSession = userAuthService.login(user) ?: return LoginErrors.AuthenticationFailedError(userName.value)

        user.setAccessToken(authSession.accessToken, authSession.refreshToken)
        domainEvents.publishAll(user)

        // 返回登录响应
        val loginResponseDto = LoginDto.Response(
            userId = user.userId.stringValue,
            accessToken = authSession.accessToken,
            refreshToken = authSession.refreshToken,
        )

        return Result.success(loginResponseDto)
    }
}