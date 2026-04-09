package dev.saraki.wofuf.modules.forum.useCases.admin.posts.featurePost

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ForumApiConstantV1.Admin.Posts.FEATURE)
class FeaturePostController(
    private val featurePostUseCase: FeaturePostUseCase
) : BaseController() {

    @PostMapping
    fun featurePost(@PathVariable postId: String): ApiResponse<FeaturePostDto.Response> {
        val result = featurePostUseCase.execute(
            FeaturePostDto.Request(postId = postId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
