package dev.saraki.wofuf.modules.forum.useCases.admin.images.getImageUrl

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ForumApiConstantV1.Admin.ROOT)
class GetImageUrlController(
    private val getImageUrlUseCase: GetImageUrlUseCase
) : BaseController() {

    @GetMapping("/images/{imageId}/url")
    fun getImageUrl(@PathVariable imageId: String): ApiResponse<GetImageUrlDto.Response> {
        val result = getImageUrlUseCase.execute(GetImageUrlDto.Request(imageId = imageId))
        return if (result.isSuccess) {
            ApiResponse.success(result.getOrThrow())
        } else {
            ApiResponse.error(result.exceptionOrNull()?.message ?: "Failed to get image URL")
        }
    }
}