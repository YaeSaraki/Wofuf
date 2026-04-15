package dev.saraki.wofuf.modules.forum.useCases.admin.images.getImages

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ForumApiConstantV1.Admin.ROOT)
class GetImagesController(
    private val getImagesUseCase: GetImagesUseCase
) : BaseController() {

    @GetMapping("/images")
    fun getImages(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) folder: String?,
        @RequestParam(required = false) uploaderId: String?
    ): ApiResponse<GetImagesDto.Response> {
        val result = getImagesUseCase.execute(
            GetImagesDto.Request(page = page, size = size, folder = folder, uploaderId = uploaderId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
