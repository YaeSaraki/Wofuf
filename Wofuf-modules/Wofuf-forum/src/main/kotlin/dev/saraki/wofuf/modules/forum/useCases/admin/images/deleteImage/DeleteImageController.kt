package dev.saraki.wofuf.modules.forum.useCases.admin.images.deleteImage

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ForumApiConstantV1.Admin.ROOT)
class DeleteImageController(
    private val deleteImageUseCase: DeleteImageUseCase
) : BaseController() {

    @DeleteMapping("/images/{imageId}")
    fun deleteImage(@PathVariable imageId: String): ApiResponse<DeleteImageDto.Response> {
        val result = deleteImageUseCase.execute(DeleteImageDto.Request(imageId = imageId))
        return if (result.isSuccess) {
            ApiResponse.success(result.getOrThrow())
        } else {
            ApiResponse.error(result.exceptionOrNull()?.message ?: "Delete failed")
        }
    }
}
