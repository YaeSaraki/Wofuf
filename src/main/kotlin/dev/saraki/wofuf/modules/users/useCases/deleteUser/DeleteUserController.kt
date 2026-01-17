package dev.saraki.wofuf.modules.users.useCases.deleteUser

import dev.saraki.wofuf.modules.users.useCases.createUser.CreateUserDto
import dev.saraki.wofuf.modules.users.useCases.createUser.CreateUserUseCase
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/17 16:31
 *   @description:
 */
@RestController
@RequestMapping("/api/v1/users/delete")
class DeleteUserController: BaseController() {
    @Autowired
    private lateinit var deleteUserUseCase: DeleteUserUseCase

    @PostMapping("")
    fun deleteUser(@RequestBody request: DeleteUserDto): ApiResponse<String> {
        val result = deleteUserUseCase.execute(request)
        if (result.isFailure) {
            return ApiResponse.error(result.exceptionOrNull()?.message ?: "Unknown error")
        }
        return ApiResponse.success("User deleted")
    }

}