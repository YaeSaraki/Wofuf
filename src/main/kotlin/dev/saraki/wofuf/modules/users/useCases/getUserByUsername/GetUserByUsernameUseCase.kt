package dev.saraki.wofuf.modules.users.useCases.getUserByUsername

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/17 21:52
 *   @description:
 */

import dev.saraki.wofuf.modules.users.dtos.UserDto
import dev.saraki.wofuf.modules.users.mappers.UserMap
import dev.saraki.wofuf.modules.users.repos.IUserRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class GetUserByUsernameUseCase: UseCase<GetUserByUsernameDto, UserDto> {
    @Autowired
    private lateinit var userRepo: IUserRepo

    override fun execute(request: GetUserByUsernameDto): Result<UserDto> {
        val userEntity = userRepo.getUserByUsername(request.username)
        if (!userEntity.isPresent) {
            return GetUserByUsernameErrors.UserNotFoundError(request.username)
        }
        val userDto = UserMap.from(userEntity.get())
        return Result.success(userDto)
    }
}