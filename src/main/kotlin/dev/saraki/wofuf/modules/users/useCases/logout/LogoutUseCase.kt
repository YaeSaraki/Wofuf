package dev.saraki.wofuf.modules.users.useCases.logout

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 21:35
 *   @description:
 */

import dev.saraki.wofuf.modules.users.mappers.UserMap
import dev.saraki.wofuf.modules.users.infra.repos.IUserRepo
import dev.saraki.wofuf.modules.users.services.auth.IAuth
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service

@Service
class LogoutUseCase(val userRepo: IUserRepo, val authService: IAuth) : UseCase<LogoutDto.LogoutRequest, LogoutDto.LogoutResponse> {
    override fun execute(request: LogoutDto.LogoutRequest): Result<LogoutDto.LogoutResponse> {

        // 验证访问令牌
        val authSession = authService.authenticate(request.accessToken)
        if (authSession == null || authSession.userId != request.id) {
            return LogoutErrors.InvalidTokenError()
        }

        // 查找用户实体
        val userEntityOrNull = userRepo.findUserById(request.id).orElse(null)
        if (userEntityOrNull == null) {
            return LogoutErrors.UserNotFoundError(request.id)
        }
        val userEntity = userEntityOrNull
        val user = UserMap.from(userEntity).toDomain()

        // 注销用户
        authService.logout(request.accessToken)

        // 清除用户的访问令牌
        user.setAccessToken("", "")

        // 返回注销响应
        val logoutResponseDto = LogoutDto.LogoutResponse(
            message = "Successfully logged out",
        )
        return Result.success(logoutResponseDto)
    }
}