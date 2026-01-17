package dev.saraki.wofuf.modules.users.useCases.createUser

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/16 17:27
 *   @description:
 */

import dev.saraki.wofuf.shared.core.AppError
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users/create")
class CreateUserController: BaseController() {
    @Autowired
    private lateinit var createUserUseCase: CreateUserUseCase

    @PostMapping("")
    fun createUser(@RequestBody request: CreateUserDto): ApiResponse<String> {
        val result = createUserUseCase.execute(request)
        if (result.isFailure) {
            val error = result.exceptionOrNull() as AppError
            return ApiResponse.error(error)
        }
        return ApiResponse.success("User created")
    }

}