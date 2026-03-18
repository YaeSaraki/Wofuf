package dev.saraki.wofuf.modules.forum.useCases.images.uploadImage

import dev.saraki.wofuf.modules.forum.config.ForumApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * 图片上传Controller
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 */
@RestController
@RequestMapping(ForumApiConstantV1.Images.ROOT)
class UploadImageController(
    private val uploadImageUseCase: UploadImageUseCase
) : BaseController() {

    @PostMapping("/upload", consumes = ["multipart/form-data"])
    fun uploadImage(
        @RequestParam("file") file: MultipartFile,
        @RequestParam(value = "folder", defaultValue = "posts") folder: String
    ): ApiResponse<UploadImageDto.Response> {
        val result = uploadImageUseCase.execute(
            UploadImageUseCase.Request(file, folder)
        ).getOrThrow()

        return ApiResponse.success(result)
    }
}
