package dev.saraki.wofuf.modules.forum.useCases.admin.members.grantPermission

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint

class GrantPermissionDto {
    data class Request(
        val memberId: String,
        val permission: PermissionPoint
    )
    data class Response(
        val memberId: String,
        val permission: String,
        val granted: Boolean,
        val message: String = "Permission granted successfully"
    )
}
