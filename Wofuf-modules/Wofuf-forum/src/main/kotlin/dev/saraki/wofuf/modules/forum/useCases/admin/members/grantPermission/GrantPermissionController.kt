package dev.saraki.wofuf.modules.forum.useCases.admin.members.grantPermission

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Members.PERMISSIONS)
class GrantPermissionController(
    private val grantPermissionUseCase: GrantPermissionUseCase
) : BaseController() {

    @Autowired
    private lateinit var memberRepo: MemberRepo

    @PostMapping
    fun grantPermission(
        @PathVariable memberId: String,
        @RequestParam permission: PermissionPoint
    ): ApiResponse<GrantPermissionDto.Response> {
        // 从 SecurityContextHolder 获取当前用户的 userId
        val authentication = SecurityContextHolder.getContext().authentication
        val userIdString = authentication?.principal as? String
            ?: throw IllegalStateException("用户未登录")

        // 查找当前用户对应的 member
        val userId = UserId.create(UniqueEntityId(userIdString)).getOrThrow()
        val member = memberRepo.findMemberByUserId(userId)
            ?: throw IllegalStateException("用户信息不存在")

        val result = grantPermissionUseCase.execute(
            GrantPermissionDto.Request(memberId = memberId, permission = permission, operatorMemberId = member.memberId.stringValue)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
