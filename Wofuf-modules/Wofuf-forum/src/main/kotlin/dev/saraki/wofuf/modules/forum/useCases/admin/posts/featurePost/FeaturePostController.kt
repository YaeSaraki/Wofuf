package dev.saraki.wofuf.modules.forum.useCases.admin.posts.featurePost

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
@RequestMapping(ForumApiConstantV1.Admin.Posts.FEATURE)
class FeaturePostController(
    private val featurePostUseCase: FeaturePostUseCase
) : BaseController() {

    @Autowired
    private lateinit var memberRepo: MemberRepo

    @PostMapping
    fun featurePost(@PathVariable postId: String): ApiResponse<FeaturePostDto.Response> {
        // 从 SecurityContextHolder 获取当前用户的 userId
        val authentication = SecurityContextHolder.getContext().authentication
        val userIdString = authentication?.principal as? String
            ?: throw IllegalStateException("用户未登录")

        // 查找当前用户对应的 member
        val userId = UserId.create(UniqueEntityId(userIdString)).getOrThrow()
        val member = memberRepo.findMemberByUserId(userId)
            ?: throw IllegalStateException("用户信息不存在")

        val result = featurePostUseCase.execute(
            FeaturePostDto.Request(postId = postId, operatorMemberId = member.memberId.stringValue)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
