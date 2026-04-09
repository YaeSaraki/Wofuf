package dev.saraki.wofuf.modules.forum.useCases.admin.members.banMember

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Members.BAN)
class BanMemberController(
    private val banMemberUseCase: BanMemberUseCase
) : BaseController() {

    @Autowired
    private lateinit var memberRepo: MemberRepo

    @PostMapping
    fun banMember(
        @PathVariable memberId: String,
        @RequestParam(required = false) reason: String?,
        @RequestParam(required = false) bannedUntilMinutes: Int?
    ): ApiResponse<BanMemberDto.Response> {
        // 从 SecurityContextHolder 获取当前用户的 userId
        val authentication = SecurityContextHolder.getContext().authentication
        val userIdString = authentication?.principal as? String
            ?: throw IllegalStateException("用户未登录")

        // 查找当前用户对应的 member
        val userId = UserId.create(UniqueEntityId(userIdString)).getOrThrow()
        val operator = memberRepo.findMemberByUserId(userId)
            ?: throw IllegalStateException("用户信息不存在")

        val result = banMemberUseCase.execute(
            BanMemberDto.Request(
                memberId = memberId,
                bannedByMemberId = operator.memberId.stringValue,
                reason = reason,
                bannedUntilMinutes = bannedUntilMinutes
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
