package dev.saraki.wofuf.modules.forum.useCases.comments.getCommentByPostSlug

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/15 16:12
 *   @description:
 */
@RestController
@RequestMapping(ForumApiConstantV1.Comments.BY_POST_SLUG)
class GetCommentByPostSlugController(
    private val getCommentByPostSlugUseCase: GetCommentByPostSlugUseCase
) : BaseController() {

    @GetMapping()
    fun getCommentByPostSlug(
        @PathVariable postSlug: String,
        @RequestParam(required = false) userId: String?
    ): ApiResponse<GetCommentByPostSlugDto.Response> {
        val result = getCommentByPostSlugUseCase.execute(
            GetCommentByPostSlugDto.Request(
                postSlug = postSlug,
                userId = userId,
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
