package dev.saraki.wofuf.modules.forum.useCases.images.listImages

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ForumApiConstantV1.Images.ROOT)
class ListImagesController(
    private val listImagesUseCase: ListImagesUseCase
) : BaseController() {

    @GetMapping("/list")
    fun listImages(
        @RequestParam(required = false) folder: String?,
        @RequestParam(required = false) uploaderMemberId: String?
    ): ApiResponse<ListImagesDto.Response> {
        val result = listImagesUseCase.execute(
            ListImagesDto.Request(folder = folder, uploaderMemberId = uploaderMemberId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
