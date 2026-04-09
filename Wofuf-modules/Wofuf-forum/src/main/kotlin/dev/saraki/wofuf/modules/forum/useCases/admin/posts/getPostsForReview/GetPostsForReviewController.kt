package dev.saraki.wofuf.modules.forum.useCases.admin.posts.getPostsForReview

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Posts.FOR_REVIEW)
class GetPostsForReviewController(
    private val getPostsForReviewUseCase: GetPostsForReviewUseCase
) : BaseController() {

    @GetMapping
    fun getPostsForReview(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<GetPostsForReviewDto.Response> {
        val result = getPostsForReviewUseCase.execute(
            GetPostsForReviewDto.Request(page = page, size = size)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
