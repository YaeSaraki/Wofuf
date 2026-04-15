package dev.saraki.wofuf.modules.forum.useCases.images.uploadImage

import dev.saraki.wofuf.auth.infra.JwtAuthFilter
import dev.saraki.wofuf.modules.forum.domain.Image
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

/**
 * 图片上传UseCase
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 */
@Service
class UploadImageUseCase(
    private val imageStorageService: ImageStorageService,
    private val imageRepo: ImageRepo,
    private val memberRepo: MemberRepo
) : UseCase<UploadImageUseCase.Request, UploadImageDto.Response> {

    private val logger = LoggerFactory.getLogger(UploadImageUseCase::class.java)

    /**
     * 请求包装类，包含文件和文件夹信息
     */
    data class Request(
        val file: MultipartFile,
        val folder: String = "posts"
    )

    override fun execute(request: Request): Result<UploadImageDto.Response> {
        val file = request.file
        val baseFolder = request.folder

        // 验证文件是否为空
        if (file.isEmpty) {
            return UploadImageErrors.FileEmptyError()
        }

        try {
            // 计算文件MD5
            val fileBytes = file.bytes
            val md5 = calculateMD5(fileBytes)

            // 先检查数据库是否已存在相同MD5的图片（真正的全局去重）
            val existingImage = imageRepo.findByMd5(md5)
            if (existingImage != null) {
                logger.info("Image with MD5 $md5 already exists in database, returning existing URL: ${existingImage.url}")
                val markdown = "![${file.originalFilename ?: "image"}](${existingImage.url})"
                return Result.success(
                    UploadImageDto.Response(
                        url = existingImage.url,
                        markdown = markdown,
                        md5 = md5,
                        isDuplicate = true
                    )
                )
            }

            // 获取当前用户ID并构建成员专属文件夹
            val memberId = getCurrentMemberId()
            val folder = if (memberId != null) "members/$memberId/$baseFolder" else baseFolder

            // 上传图片到MinIO
            val uploadResult = imageStorageService.uploadImage(file, folder)

            // 保存图片元数据到数据库
            val image = Image.create(
                url = uploadResult.url,
                md5 = md5,
                folder = folder,
                uploaderId = memberId,
                fileSize = file.size,
                contentType = file.contentType ?: "image/jpeg",
                fileName = file.originalFilename ?: "image"
            )
            imageRepo.save(image)
            logger.info("Image uploaded successfully (MD5: $md5, folder: $folder)")

            // 生成Markdown格式
            val markdown = "![${file.originalFilename ?: "image"}](${uploadResult.url})"

            return Result.success(
                UploadImageDto.Response(
                    url = uploadResult.url,
                    markdown = markdown,
                    md5 = md5,
                    isDuplicate = false
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

    /**
     * 获取当前成员的ID
     */
    private fun getCurrentMemberId(): String? {
        val userId = JwtAuthFilter.getCurrentUserId() ?: return null
        return try {
            val userIdVo = UserId.create(UniqueEntityId(userId)).getOrNull() ?: return null
            memberRepo.findMemberByUserId(userIdVo)?.memberId?.stringValue
        } catch (e: Exception) {
            logger.warn("Failed to get member ID for userId: $userId", e)
            null
        }
    }

    /**
     * 计算文件的MD5哈希值
     */
    private fun calculateMD5(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
