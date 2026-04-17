package dev.saraki.wofuf.modules.forum.useCases.admin.members.revokePermission

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint

class RevokePermissionDto {
    data class Request(
        val memberId: String,
        val permission: PermissionPoint,
        val operatorMemberId: String,
    )
    data class Response(
        val memberId: String,
        val permission: String,
        val revoked: Boolean,
        val message: String = "Permission revoked successfully"
    )
}
