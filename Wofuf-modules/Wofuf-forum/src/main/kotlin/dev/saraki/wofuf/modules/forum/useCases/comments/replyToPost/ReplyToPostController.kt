package dev.saraki.wofuf.modules.forum.useCases.comments.replyToPost

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15 16:15
 * @description Reply to a post directly (not to a comment)
 */
@RestController
class ReplyToPostController(
    private val replyToPostUseCase: ReplyToPostUseCase
) : BaseController() {

    /**
     * 通过帖子 UUID 回复
     */
    @PostMapping(ForumApiConstantV1.Posts.REPLIES)
    fun replyToPost(
        @PathVariable postId: String,
        @RequestBody request: ReplyToPostRequest
    ): ApiResponse<ReplyToPostDto.Response> {
        val result = replyToPostUseCase.execute(
            ReplyToPostDto.Request(
                postSlug = null,
                postId = postId,
                userId = request.userId,
                comment = request.comment,
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }

    /**
     * 通过帖子 Slug 回复
     */
    @PostMapping(ForumApiConstantV1.Posts.REPLIES_BY_SLUG)
    fun replyToPostBySlug(
        @PathVariable postSlug: String,
        @RequestBody request: ReplyToPostRequest
    ): ApiResponse<ReplyToPostDto.Response> {
        val result = replyToPostUseCase.execute(
            ReplyToPostDto.Request(
                postSlug = postSlug,
                postId = null,
                userId = request.userId,
                comment = request.comment,
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}

data class ReplyToPostRequest(
    val userId: String,
    val comment: String,
)
