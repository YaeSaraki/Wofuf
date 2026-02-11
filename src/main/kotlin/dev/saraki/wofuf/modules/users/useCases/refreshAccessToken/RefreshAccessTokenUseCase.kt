package dev.saraki.wofuf.modules.users.useCases.refreshAccessToken

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
class RefreshAccessTokenUseCase(
    val userRepo: UserRepo,
    val authService: IAuth
) : UseCase<RefreshAccessTokenDto.Request, RefreshAccessTokenDto.Response> {
    override fun execute(request: RefreshAccessTokenDto.Request): Result<RefreshAccessTokenDto.Response> {

        // 验证访问令牌
        val authSession = authService.authenticate(request.accessToken)
        if (authSession == null || authSession.userId != request.id) {
            return RefreshAccessTokenErrors.InvalidTokenError()
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
            return RefreshAccessTokenErrors.UserNotFoundError(request.id)
        }

        // service 生成新的访问令牌和刷新令牌
        val refreshAuthSession = authService.refresh(request.refreshToken)
        if (refreshAuthSession == null) {
            return RefreshAccessTokenErrors.InvalidTokenError()
        }

        // 返回刷新令牌响应
        val refreshAccessTokenResponseDto = RefreshAccessTokenDto.Response(
            accessToken = refreshAuthSession.accessToken,
            refreshToken = refreshAuthSession.refreshToken,
        )
        return Result.success(refreshAccessTokenResponseDto)
    }
}