package dev.saraki.wofuf.modules.forum.useCases.admin.images.getImageUrl

import dev.saraki.wofuf.modules.forum.infra.repos.ImageRepo
import dev.saraki.wofuf.modules.forum.infra.storage.ImageStorageService
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service

@Service
class GetImageUrlUseCase(
    private val imageRepo: ImageRepo,
    private val imageStorageService: ImageStorageService
) : UseCase<GetImageUrlDto.Request, GetImageUrlDto.Response> {

    override fun execute(request: GetImageUrlDto.Request): Result<GetImageUrlDto.Response> {
        val image = imageRepo.findById(request.imageId)
            ?: return Result.failure("Image not found: ${request.imageId}")

        val url = imageStorageService.generatePresignedUrl(image.objectName)

        return Result.success(GetImageUrlDto.Response(
            url = url,
            expiresInSeconds = 300
        ))
    }
}