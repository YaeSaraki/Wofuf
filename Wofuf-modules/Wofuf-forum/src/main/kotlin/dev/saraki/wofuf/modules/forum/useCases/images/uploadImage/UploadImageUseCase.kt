package dev.saraki.wofuf.modules.forum.useCases.images.uploadImage

import dev.saraki.wofuf.auth.infra.JwtAuthFilter
import dev.saraki.wofuf.modules.forum.domain.Image
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.infra.repos.ImageRepo
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.storage.ImageStorageService
import dev.saraki.wofuf.modules.forum.infra.storage.ImageUploadException
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.security.MessageDigest

@Service
class UploadImageUseCase(
    private val imageStorageService: ImageStorageService,
    private val imageRepo: ImageRepo,
    private val memberRepo: MemberRepo
) : UseCase<UploadImageUseCase.Request, UploadImageDto.Response> {

    private val logger = LoggerFactory.getLogger(UploadImageUseCase::class.java)

    data class Request(
        val file: MultipartFile,
        val folder: String = "posts"
    )

    override fun execute(request: Request): Result<UploadImageDto.Response> {
        val file = request.file
        val baseFolder = request.folder

        if (file.isEmpty) {
            return UploadImageErrors.FileEmptyError()
        }

        try {
            val fileBytes = file.bytes
            val md5 = calculateMD5(fileBytes)

            val existingImage = imageRepo.findByMd5(md5)
            if (existingImage != null) {
                if (existingImage.objectName.isNotBlank()) {
                    // 有有效objectName，返回干净的URL
                    val cleanUrl = "/api/v1/forum/images/" + md5
                    logger.info("Image with MD5 $md5 already exists in database, returning clean URL")
                    val markdown = "![" + (file.originalFilename ?: "image") + "](" + cleanUrl + ")"
                    return Result.success(
                        UploadImageDto.Response(
                            url = cleanUrl,
                            markdown = markdown,
                            md5 = md5,
                            isDuplicate = true
                        )
                    )
                } else {
                    // 有记录但objectName为空（旧数据），需要更新
                    logger.info("Image with MD5 $md5 exists but has empty objectName, will update")
                }
            }

            val memberId = getCurrentMemberId()
            val folder = if (memberId != null) "members/${memberId.stringValue}/$baseFolder" else baseFolder

            val uploadResult = imageStorageService.uploadImage(file, folder)

            val image = if (existingImage != null) {
                // 更新已有记录
                Image.createWithId(
                    id = dev.saraki.wofuf.shared.domain.UniqueEntityId(existingImage.imageId),
                    objectName = uploadResult.objectName,
                    md5 = md5,
                    folder = folder,
                    uploaderId = memberId ?: existingImage.uploaderId,
                    uploadedAt = existingImage.uploadedAt,
                    fileSize = file.size,
                    contentType = file.contentType ?: "image/jpeg",
                    fileName = file.originalFilename ?: existingImage.fileName
                )
            } else {
                // 创建新记录
                Image.create(
                    objectName = uploadResult.objectName,
                    md5 = md5,
                    folder = folder,
                    uploaderId = memberId,
                    fileSize = file.size,
                    contentType = file.contentType ?: "image/jpeg",
                    fileName = file.originalFilename ?: "image"
                )
            }
            imageRepo.save(image)
            logger.info("Image uploaded successfully (MD5: $md5, folder: $folder)")

            // 返回干净的URL格式
            val cleanUrl = "/api/v1/forum/images/" + md5
            val markdown = "![" + (file.originalFilename ?: "image") + "](" + cleanUrl + ")"

            return Result.success(
                UploadImageDto.Response(
                    url = cleanUrl,
                    markdown = markdown,
                    md5 = md5,
                    isDuplicate = existingImage != null
                )
            )
        } catch (e: ImageUploadException) {
            logger.error("Image upload failed: ${e.message}")
            return when {
                e.message?.contains("不能为空") == true -> UploadImageErrors.FileEmptyError()
                e.message?.contains("不能超过") == true -> UploadImageErrors.FileTooLargeError()
                e.message?.contains("不支持") == true -> UploadImageErrors.UnsupportedFileTypeError(file.contentType)
                else -> UploadImageErrors.UploadFailedError(e.message)
            }
        } catch (e: Exception) {
            logger.error("Unexpected error during image upload", e)
            return UploadImageErrors.UploadFailedError(e.message)
        }
    }

    private fun getCurrentMemberId(): MemberId? {
        val userId = JwtAuthFilter.getCurrentUserId() ?: return null
        return try {
            val userIdVo = UserId.create(UniqueEntityId(userId)).getOrNull() ?: return null
            memberRepo.findMemberByUserId(userIdVo)?.memberId
        } catch (e: Exception) {
            logger.warn("Failed to get member ID for userId: $userId", e)
            null
        }
    }

    private fun calculateMD5(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
