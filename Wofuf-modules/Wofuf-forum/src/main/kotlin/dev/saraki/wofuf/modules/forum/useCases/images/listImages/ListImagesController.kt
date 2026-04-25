package dev.saraki.wofuf.modules.forum.useCases.images.listImages

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ForumApiConstantV1.Images.ROOT)
class ListImagesController(
    private val listImagesUseCase: ListImagesUseCase,
    private val memberRepo: MemberRepo
) : BaseController() {

    @GetMapping("/list")
    fun listImages(
        @RequestParam(required = false) folder: String?
    ): ApiResponse<ListImagesDto.Response> {
        // 从 SecurityContextHolder 获取当前登录用户的 memberId
        val authentication = SecurityContextHolder.getContext().authentication
        val userIdString = authentication?.principal as? String
            ?: throw IllegalStateException("用户未登录")

        // 查找当前用户对应的 member
        val userId = UserId.create(UniqueEntityId(userIdString)).getOrThrow()
        val member = memberRepo.findMemberByUserId(userId)
            ?: throw IllegalStateException("用户信息不存在")

        // 使用当前用户的 memberId 查询图片
        val result = listImagesUseCase.execute(
            ListImagesDto.Request(folder = folder, uploaderMemberId = member.memberId.stringValue)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
