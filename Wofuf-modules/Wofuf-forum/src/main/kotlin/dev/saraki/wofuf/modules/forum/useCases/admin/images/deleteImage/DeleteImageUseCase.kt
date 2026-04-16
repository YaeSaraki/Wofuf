package dev.saraki.wofuf.modules.forum.useCases.admin.images.deleteImage

import dev.saraki.wofuf.modules.forum.domain.OperationType
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.ImageRepo
import dev.saraki.wofuf.modules.forum.infra.services.OperationLogService
import dev.saraki.wofuf.modules.forum.infra.storage.ImageStorageService
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DeleteImageUseCase(
    private val imageRepo: ImageRepo,
    private val imageStorageService: ImageStorageService,
    private val operationLogService: OperationLogService
) : UseCase<DeleteImageDto.Request, DeleteImageDto.Response> {

    private val logger = LoggerFactory.getLogger(DeleteImageUseCase::class.java)

    @RequirePermission(PermissionPoint.ADMIN_ACCESS, "Only administrators can delete images")
    override fun execute(request: DeleteImageDto.Request): Result<DeleteImageDto.Response> {
        val imageId = request.imageId

        if (request.operatorMemberId.isBlank()) {
            return Result.failure("Operator member ID is required")
        }

        val operatorMemberIdOrError = MemberId.create(UniqueEntityId(request.operatorMemberId))
        if (operatorMemberIdOrError.isFailure) {
            return Result.failure("Invalid operator member ID")
        }
        val operatorMemberId = operatorMemberIdOrError.getOrThrow()

        // Find image by ID
        val image = imageRepo.findById(imageId)
            ?: return Result.failure("Image not found: $imageId")

        try {
            // Delete from MinIO storage using stored objectName
            imageStorageService.deleteImage(image.objectName)

            // Delete from database
            imageRepo.delete(image.md5)

            // 记录操作日志
            operationLogService.logImageAction(
                operationType = OperationType.IMAGE_DELETE,
                imageId = imageId,
                operatorId = operatorMemberId,
                details = "Deleted image: ${image.fileName} (${image.md5})"
            )

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
