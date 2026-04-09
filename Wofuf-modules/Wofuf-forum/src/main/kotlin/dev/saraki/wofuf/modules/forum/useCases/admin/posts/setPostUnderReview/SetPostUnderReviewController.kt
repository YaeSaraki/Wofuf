package dev.saraki.wofuf.modules.forum.useCases.admin.posts.setPostUnderReview

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Posts.REVIEW)
class SetPostUnderReviewController(
    private val setPostUnderReviewUseCase: SetPostUnderReviewUseCase
) : BaseController() {

    @PostMapping
    fun setPostUnderReview(@PathVariable postId: String): ApiResponse<SetPostUnderReviewDto.Response> {
        val result = setPostUnderReviewUseCase.execute(
            SetPostUnderReviewDto.Request(postId = postId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
