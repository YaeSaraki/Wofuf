package dev.saraki.wofuf.modules.users.useCases.getUserByUsername

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/17 21:51
 *   @description:
 */

import dev.saraki.wofuf.modules.users.dtos.UserDto
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users/{username}")
class GetUserByUsernameController: BaseController() {
    @Autowired
    private lateinit var getUserByUsernameUseCase: GetUserByUsernameUseCase

    @GetMapping("")
    fun getUserByUsername(@PathVariable username: String): ApiResponse<UserDto> {
        val resultOrError = getUserByUsernameUseCase.execute(GetUserByUsernameDto(username))
        if (resultOrError.isFailure) {
            return ApiResponse.error(resultOrError.exceptionOrNull()?.message ?: "Unknown error")
        }
        val result = resultOrError.getOrThrow()
        return ApiResponse.success(result)
    }
}