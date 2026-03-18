package dev.saraki.wofuf.modules.forum.infra.storage

import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.StatObjectArgs
import io.minio.errors.ErrorResponseException
import io.minio.http.Method
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * 图片存储服务
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 */
@Service
class ImageStorageService(
    private val minioClient: MinioClient,
    private val minioProperties: MinioProperties
) {
    private val logger = LoggerFactory.getLogger(ImageStorageService::class.java)

    // 允许的图片类型
    private val allowedContentTypes = setOf(
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp"
    )

    // 最大文件大小 10MB
    private val maxFileSize = 10 * 1024 * 1024L

    /**
     * 上传结果
     */
    data class UploadResult(
        val url: String,
        val md5: String,
        val isDuplicate: Boolean = false
    )

    /**
     * 上传图片到MinIO
     * @param file 上传的文件
     * @param folder 存储文件夹 (如 "posts", "avatars")
     * @return 上传结果，包含URL和MD5
     */
    fun uploadImage(file: MultipartFile, folder: String = "posts"): UploadResult {
        // 验证文件
        validateImage(file)

        // 计算文件MD5
        val fileBytes = file.bytes
        val md5 = calculateMD5(fileBytes)

        // 获取文件扩展名
        val originalFilename = file.originalFilename ?: "image"
        val extension = getFileExtension(originalFilename)

        // 使用MD5作为文件名，防止重复上传
        val objectName = "$folder/${md5}${extension}"

        try {
            // 检查文件是否已存在
            if (imageExists(objectName)) {
                logger.info("Image already exists, returning existing URL: $objectName")
                return UploadResult(
                    url = getPresignedUrl(objectName),
                    md5 = md5,
                    isDuplicate = true
                )
            }

            // 上传新文件
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioProperties.bucketName)
                    .`object`(objectName)
                    .stream(fileBytes.inputStream(), fileBytes.size.toLong(), -1)
                    .contentType(file.contentType)
                    .build()
            )

            logger.info("Uploaded image: $objectName (MD5: $md5)")

            return UploadResult(
                url = getPresignedUrl(objectName),
                md5 = md5,
                isDuplicate = false
            )
        } catch (e: Exception) {
            logger.error("Failed to upload image: ${e.message}")
            throw ImageUploadException("图片上传失败: ${e.message}")
        }
    }

    /**
     * 检查图片是否已存在
     */
    private fun imageExists(objectName: String): Boolean {
        return try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(minioProperties.bucketName)
                    .`object`(objectName)
                    .build()
            )
            true
        } catch (e: ErrorResponseException) {
            false
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

    /**
     * 获取图片的预签名URL (有效期7天)
     */
    private fun getPresignedUrl(objectName: String): String {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(minioProperties.bucketName)
                .`object`(objectName)
                .expiry(7, TimeUnit.DAYS)
                .build()
        )
    }

    /**
     * 验证图片文件
     */
    private fun validateImage(file: MultipartFile) {
        if (file.isEmpty) {
            throw ImageUploadException("文件不能为空")
        }

        if (file.size > maxFileSize) {
            throw ImageUploadException("文件大小不能超过10MB")
        }

        val contentType = file.contentType
        if (contentType == null || contentType !in allowedContentTypes) {
            throw ImageUploadException("不支持的文件类型，仅支持 JPEG, PNG, GIF, WebP")
        }
    }

    /**
     * 获取文件扩展名
     */
    private fun getFileExtension(filename: String): String {
        val lastDot = filename.lastIndexOf('.')
        return if (lastDot > 0) filename.substring(lastDot) else ".jpg"
    }

    /**
     * 检查URL是否为外部图片URL
     */
    fun isExternalImageUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    /**
     * 验证外部图片URL是否有效
     */
    fun validateExternalImageUrl(url: String): Boolean {
        // 基本URL格式验证
        val urlPattern = Regex("^https?://.+\\.(jpg|jpeg|png|gif|webp)(\\?.*)?$", RegexOption.IGNORE_CASE)
        return urlPattern.matches(url)
    }
}

/**
 * 图片上传异常
 */
class ImageUploadException(message: String) : RuntimeException(message)
