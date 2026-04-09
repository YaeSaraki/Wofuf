package dev.saraki.wofuf.modules.forum.useCases.admin.members.grantPermission

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Members.PERMISSIONS)
class GrantPermissionController(
    private val grantPermissionUseCase: GrantPermissionUseCase
) : BaseController() {

    @PostMapping
    fun grantPermission(
        @PathVariable memberId: String,
        @RequestParam permission: PermissionPoint
    ): ApiResponse<GrantPermissionDto.Response> {
        val result = grantPermissionUseCase.execute(
            GrantPermissionDto.Request(memberId = memberId, permission = permission)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
