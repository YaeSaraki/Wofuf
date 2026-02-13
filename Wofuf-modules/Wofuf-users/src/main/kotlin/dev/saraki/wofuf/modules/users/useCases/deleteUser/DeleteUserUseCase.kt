package dev.saraki.wofuf.modules.users.useCases.deleteUser

import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.modules.users.infra.repos.UserRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.domain.events.IDomainEvents
import org.springframework.stereotype.Service

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/17 16:32
 *   @description:
 */
@Service
class DeleteUserUseCase(
    private val userRepo: UserRepo,
    private val domainEvents: IDomainEvents
) : UseCase<DeleteUserDto.Request, Unit> {
    override fun execute(request: DeleteUserDto.Request): Result<Unit> {

        // 检测UserId是否有效
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return Result.failure(userIdOrError.exceptionOrThrow())
        }
        val userId = userIdOrError.getOrThrow()

        // 查找用户实体
        val user = userRepo.findUserByUserId(userId) ?: return DeleteUserErrors.UserNotFoundError(request.userId)

        user.delete()
        domainEvents.publishAll(user)

        userRepo.save(user)
        return Result.success(Unit)
    }
}
