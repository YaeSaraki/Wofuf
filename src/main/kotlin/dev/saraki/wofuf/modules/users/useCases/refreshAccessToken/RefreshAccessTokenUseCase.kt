package dev.saraki.wofuf.modules.users.useCases.refreshAccessToken

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
class RefreshAccessTokenUseCase(val userRepo: IUserRepo, val authService: IAuth) : UseCase<RefreshAccessTokenDto.RefreshAccessTokenRequest, RefreshAccessTokenDto.RefreshAccessTokenResponse> {
    override fun execute(request: RefreshAccessTokenDto.RefreshAccessTokenRequest): Result<RefreshAccessTokenDto.RefreshAccessTokenResponse> {

        // 验证访问令牌
        val authSession = authService.authenticate(request.accessToken)
        if (authSession == null || authSession.userId != request.id) {
            return RefreshAccessTokenErrors.InvalidTokenError()
        }

        // 查找用户实体
        val userEntityOrNull = userRepo.findUserById(request.id).orElse(null)
        if (userEntityOrNull == null) {
            return RefreshAccessTokenErrors.UserNotFoundError(request.id)
        }
        val userEntity = userEntityOrNull
        val user = UserMap.from(userEntity).toDomain()

        // service 生成新的访问令牌和刷新令牌
        val refreshAuthSession = authService.refresh(request.refreshToken)
        if (refreshAuthSession == null) {
            return RefreshAccessTokenErrors.InvalidTokenError()
        }

        // 返回刷新令牌响应
        val refreshAccessTokenResponseDto = RefreshAccessTokenDto.RefreshAccessTokenResponse(
            accessToken = refreshAuthSession.accessToken,
            refreshToken = refreshAuthSession.refreshToken,
        )
        return Result.success(refreshAccessTokenResponseDto)
    }
}