package dev.saraki.wofuf.modules.forum.infra.storage

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * MinIO配置属性
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 */
@Component
@ConfigurationProperties(prefix = "minio")
data class MinioProperties(
    var endpoint: String = "http://localhost:9000",
    var accessKey: String = "minioadmin",
    var secretKey: String = "minioadmin",
    var bucketName: String = "wofuf-images"
)
