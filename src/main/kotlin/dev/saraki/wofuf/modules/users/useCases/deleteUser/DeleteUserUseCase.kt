package dev.saraki.wofuf.modules.users.useCases.deleteUser

import dev.saraki.wofuf.modules.users.mappers.UserMap
import dev.saraki.wofuf.modules.users.repos.IUserRepo
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.core.Result
import io.lettuce.core.KillArgs.Builder.user
import org.springframework.stereotype.Service

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/17 16:32
 *   @description:
 */
@Service
open class DeleteUserUseCase(val userRepo: IUserRepo): UseCase<DeleteUserDto, Unit> {
    override fun execute(request: DeleteUserDto): Result<Unit> {
        val userEntity = userRepo.getUserById(request.userId)
        if (!userEntity.isPresent) {
            return DeleteUserErrors.UserNotFoundError(request.userId)
        }
        val user = UserMap.from(userEntity.get()).toDomain()
        user.delete()
        val userEntityChanged = UserMap.from(user).toEntity()
        userRepo.save(userEntityChanged)
        return Result.success(Unit)
    }
}
