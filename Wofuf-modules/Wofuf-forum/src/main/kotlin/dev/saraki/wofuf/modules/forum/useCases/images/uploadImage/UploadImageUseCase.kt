package dev.saraki.wofuf.modules.forum.useCases.images.uploadImage

import dev.saraki.wofuf.modules.forum.domain.Image
import dev.saraki.wofuf.modules.forum.infra.repos.ImageRepo
import dev.saraki.wofuf.modules.forum.infra.storage.ImageStorageService
import dev.saraki.wofuf.modules.forum.infra.storage.ImageUploadException
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

/**
 * 图片上传UseCase
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 */
@Service
class UploadImageUseCase(
    private val imageStorageService: ImageStorageService,
    private val imageRepo: ImageRepo
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
        val folder = request.folder

        // 验证文件是否为空
        if (file.isEmpty) {
            return UploadImageErrors.FileEmptyError()
        }

        try {
            // 上传图片（自动去重）
            val uploadResult = imageStorageService.uploadImage(file, folder)

            // 如果不是重复上传，则保存图片元数据到数据库
            if (!uploadResult.isDuplicate) {
                val image = Image.create(
                    url = uploadResult.url,
                    md5 = uploadResult.md5,
                    folder = folder,
                    uploaderId = null, // TODO: 从JWT获取当前用户ID
                    fileSize = file.size,
                    contentType = file.contentType ?: "image/jpeg",
                    fileName = file.originalFilename ?: "image"
                )
                imageRepo.save(image)
                logger.info("Image metadata saved to database (MD5: ${uploadResult.md5})")
            }

            // 生成Markdown格式
            val originalFilename = file.originalFilename ?: "image"
            val markdown = "![${originalFilename}](${uploadResult.url})"

            if (uploadResult.isDuplicate) {
                logger.info("Image already exists (MD5: ${uploadResult.md5}), returning existing URL")
            } else {
                logger.info("Image uploaded successfully (MD5: ${uploadResult.md5})")
            }

            return Result.success(
                UploadImageDto.Response(
                    url = uploadResult.url,
                    markdown = markdown,
                    md5 = uploadResult.md5,
                    isDuplicate = uploadResult.isDuplicate
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
}
