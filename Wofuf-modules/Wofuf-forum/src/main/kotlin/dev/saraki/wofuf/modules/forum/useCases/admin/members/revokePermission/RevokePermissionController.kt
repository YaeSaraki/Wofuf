package dev.saraki.wofuf.modules.forum.useCases.admin.members.revokePermission

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Members.PERMISSION_BY_NAME)
class RevokePermissionController(
    private val revokePermissionUseCase: RevokePermissionUseCase
) : BaseController() {

    @DeleteMapping
    fun revokePermission(
        @PathVariable memberId: String,
        @PathVariable permission: PermissionPoint
    ): ApiResponse<RevokePermissionDto.Response> {
        val result = revokePermissionUseCase.execute(
            RevokePermissionDto.Request(memberId = memberId, permission = permission)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
