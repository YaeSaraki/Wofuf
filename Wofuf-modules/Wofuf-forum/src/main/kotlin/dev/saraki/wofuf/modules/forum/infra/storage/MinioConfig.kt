package dev.saraki.wofuf.modules.forum.infra.storage

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * MinIO配置类
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 */
@Configuration
class MinioConfig(
    private val minioProperties: MinioProperties
) {
    private val logger = LoggerFactory.getLogger(MinioConfig::class.java)

    @Bean
    fun minioClient(): MinioClient {
        val client = MinioClient.builder()
            .endpoint(minioProperties.endpoint)
            .credentials(minioProperties.accessKey, minioProperties.secretKey)
            .build()

        // 确保bucket存在
        try {
            val bucketExists = client.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(minioProperties.bucketName)
                    .build()
            )

            if (!bucketExists) {
                client.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(minioProperties.bucketName)
                        .build()
                )
                logger.info("Created MinIO bucket: ${minioProperties.bucketName}")
            }
        } catch (e: Exception) {
            logger.warn("Could not verify/create MinIO bucket: ${e.message}")
        }

        return client
    }
}
