package dev.saraki.wofuf.modules.users.useCases.logout

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 21:35
 *   @description:
 */

import dev.saraki.wofuf.modules.users.domain.UserId
import dev.saraki.wofuf.modules.users.infra.repos.UserRepo
import dev.saraki.wofuf.modules.users.services.auth.IAuth
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class LogoutUseCase(
    val userRepo: UserRepo,
    val authService: IAuth
) : UseCase<LogoutDto.Request, LogoutDto.Response> {
    override fun execute(request: LogoutDto.Request): Result<LogoutDto.Response> {

        // 验证访问令牌
        val authSession = authService.authenticate(request.accessToken)
        if (authSession == null || authSession.userId != request.id) {
            return LogoutErrors.InvalidTokenError()
        }

        // 检测UserId是否有效
        val userIdOrError = UserId.create(UniqueEntityId(request.id))
        if (userIdOrError.isFailure) {
            return Result.failure(userIdOrError.exceptionOrThrow())
        }
        val userId = userIdOrError.getOrThrow()

        // 查找用户实体
        val user = userRepo.findUserByUserId(userId)
        if (user == null) {
            return LogoutErrors.UserNotFoundError(request.id)
        }

        // 注销用户
        authService.logout(request.accessToken)

        // 清除用户的访问令牌
        user.setAccessToken("", "")

        // 返回注销响应
        val logoutResponseDto = LogoutDto.Response(
            message = "Successfully logged out",
        )
        return Result.success(logoutResponseDto)
    }
}