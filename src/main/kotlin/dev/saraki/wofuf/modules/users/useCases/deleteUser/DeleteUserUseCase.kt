package dev.saraki.wofuf.modules.users.useCases.deleteUser

import dev.saraki.wofuf.modules.users.mappers.UserMap
import dev.saraki.wofuf.modules.users.infra.repos.IUserRepo
import dev.saraki.wofuf.modules.users.services.auth.IAuth
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.core.Result
import org.springframework.stereotype.Service

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/17 16:32
 *   @description:
 */
@Service
open class DeleteUserUseCase(
    private val userRepo: IUserRepo,
    private val authService: IAuth
): UseCase<DeleteUserDto, Unit> {
    override fun execute(request: DeleteUserDto): Result<Unit> {
        // 查找用户实体
        val userEntity = userRepo.findUserById(request.userId)
        if (!userEntity.isPresent) {
            return DeleteUserErrors.UserNotFoundError(request.userId)
        }

        // 验证访问令牌
        val authSession = authService.authenticate(request.accessToken)
        if (authSession == null || authSession.userId != request.userId) {
            return DeleteUserErrors.UnauthorizedError()
        }

        val user = UserMap.from(userEntity.get()).toDomain()
        user.delete()
        val userEntityChanged = UserMap.from(user).toEntity()
        userRepo.save(userEntityChanged)
        return Result.success(Unit)
    }
}
