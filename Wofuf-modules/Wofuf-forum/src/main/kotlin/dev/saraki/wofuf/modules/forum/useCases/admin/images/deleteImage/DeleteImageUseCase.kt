package dev.saraki.wofuf.modules.forum.useCases.admin.images.deleteImage

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.ImageRepo
import dev.saraki.wofuf.modules.forum.infra.storage.ImageStorageService
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DeleteImageUseCase(
    private val imageRepo: ImageRepo,
    private val imageStorageService: ImageStorageService
) : UseCase<DeleteImageDto.Request, DeleteImageDto.Response> {

    private val logger = LoggerFactory.getLogger(DeleteImageUseCase::class.java)

    @RequirePermission(PermissionPoint.ADMIN_ACCESS, "Only administrators can delete images")
    override fun execute(request: DeleteImageDto.Request): Result<DeleteImageDto.Response> {
        val imageId = request.imageId

        // Find image by ID
        val image = imageRepo.findByMd5(imageId)
            ?: return Result.failure("Image not found: $imageId")

        try {
            // Delete from MinIO storage
            val objectName = "${image.folder}/${image.md5}${getExtension(image.contentType)}"
            imageStorageService.deleteImage(objectName)

            // Delete from database
            imageRepo.delete(image.md5)

            logger.info("Image deleted successfully: ${image.md5}")
            return Result.success(DeleteImageDto.Response(
                success = true,
                message = "Image deleted successfully"
            ))
        } catch (e: Exception) {
            logger.error("Failed to delete image: ${e.message}")
            return Result.failure("Failed to delete image: ${e.message}")
        }
    }

    private fun getExtension(contentType: String): String {
        return when (contentType) {
            "image/jpeg" -> ".jpg"
            "image/png" -> ".png"
            "image/gif" -> ".gif"
            "image/webp" -> ".webp"
            else -> ".jpg"
        }
    }
}
