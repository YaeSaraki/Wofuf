package dev.saraki.wofuf.modules.users.useCases.refreshAccessToken

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 21:35
 *   @description:
 */

import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.modules.users.infra.repos.UserRepo
import dev.saraki.wofuf.modules.users.services.auth.UserAuthService
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class RefreshAccessTokenUseCase(
    val userRepo: UserRepo,
    val userAuthService: UserAuthService
) : UseCase<RefreshAccessTokenDto.Request, RefreshAccessTokenDto.Response> {
    override fun execute(request: RefreshAccessTokenDto.Request): Result<RefreshAccessTokenDto.Response> {

        // 使用 refreshToken 刷新会话
        val refreshAuthSession = userAuthService.refresh(request.refreshToken)
            ?: return RefreshAccessTokenErrors.InvalidTokenError()

        // 从新的 JWT 中获取用户信息
        val jwtClaims = userAuthService.decodeJWT(refreshAuthSession.accessToken)
            ?: return RefreshAccessTokenErrors.InvalidTokenError()

        // 检测UserId是否有效
        val userIdOrError = UserId.create(UniqueEntityId(jwtClaims.userId))
        if (userIdOrError.isFailure) {
            return Result.failure(userIdOrError.exceptionOrThrow())
        }
        val userId = userIdOrError.getOrThrow()

        // 查找用户实体
        val user = userRepo.findUserByUserId(userId)
            ?: return RefreshAccessTokenErrors.UserNotFoundError(jwtClaims.userId)

        // 返回刷新令牌响应
        val refreshAccessTokenResponseDto = RefreshAccessTokenDto.Response(
            userId = user.userId.stringValue,
            accessToken = refreshAuthSession.accessToken,
            refreshToken = refreshAuthSession.refreshToken,
        )
        return Result.success(refreshAccessTokenResponseDto)
    }
}