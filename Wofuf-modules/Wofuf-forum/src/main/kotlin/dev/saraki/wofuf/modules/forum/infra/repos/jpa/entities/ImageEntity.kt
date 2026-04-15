package dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "forum_image",
    indexes = [
        Index(name = "idx_image_md5", columnList = "md5", unique = true),
        Index(name = "idx_image_folder", columnList = "folder"),
        Index(name = "idx_image_uploader", columnList = "uploader_id"),
        Index(name = "idx_image_uploaded_at", columnList = "uploaded_at")
    ]
)
data class ImageEntity(
    @Id
    @Column(name = "image_id", nullable = false)
    val imageId: String,

    @Column(name = "url", nullable = false, length = 1024)
    val url: String,

    @Column(name = "md5", nullable = false, unique = true, length = 32)
    val md5: String,

    @Column(name = "folder", nullable = false, length = 64)
    val folder: String,

    @Column(name = "uploader_id", nullable = true)
    val uploaderId: String? = null,

    @Column(name = "uploaded_at", nullable = false)
    val uploadedAt: Instant,

    @Column(name = "file_size", nullable = false)
    val fileSize: Long,

    @Column(name = "content_type", nullable = false, length = 64)
    val contentType: String,

    @Column(name = "file_name", nullable = false, length = 255)
    val fileName: String,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)
