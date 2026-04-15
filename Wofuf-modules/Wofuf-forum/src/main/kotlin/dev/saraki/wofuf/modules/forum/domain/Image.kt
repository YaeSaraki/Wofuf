package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.shared.domain.AggregateRoot
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import java.time.Instant

/**
 * 图片领域实体
 * @author YaeSaraki
 */
class Image private constructor(
    props: ImageProps,
    id: UniqueEntityId?,
) : AggregateRoot<ImageProps>(props, id) {

    val objectName: String get() = props.objectName
    val md5: String get() = props.md5
    val folder: String get() = props.folder
    val uploaderId: String? get() = props.uploaderId
    val uploadedAt: Instant get() = props.uploadedAt
    val fileSize: Long get() = props.fileSize
    val contentType: String get() = props.contentType
    val fileName: String get() = props.fileName
    val imageId: String get() = id?.uuid?.toString() ?: throw IllegalStateException("Image ID is null")

    companion object {
        fun create(
            objectName: String,
            md5: String,
            folder: String,
            uploaderId: String?,
            fileSize: Long,
            contentType: String,
            fileName: String,
        ): Image {
            return Image(
                props = ImageProps(
                    objectName = objectName,
                    md5 = md5,
                    folder = folder,
                    uploaderId = uploaderId,
                    uploadedAt = Instant.now(),
                    fileSize = fileSize,
                    contentType = contentType,
                    fileName = fileName,
                ),
                id = UniqueEntityId(),
            )
        }

        fun createWithId(
            id: UniqueEntityId,
            objectName: String,
            md5: String,
            folder: String,
            uploaderId: String?,
            uploadedAt: Instant,
            fileSize: Long,
            contentType: String,
            fileName: String,
        ): Image {
            return Image(
                props = ImageProps(
                    objectName = objectName,
                    md5 = md5,
                    folder = folder,
                    uploaderId = uploaderId,
                    uploadedAt = uploadedAt,
                    fileSize = fileSize,
                    contentType = contentType,
                    fileName = fileName,
                ),
                id = id,
            )
        }
    }
}

/**
 * 图片属性
 */
data class ImageProps(
    val objectName: String,
    val md5: String,
    val folder: String,
    val uploaderId: String?,
    val uploadedAt: Instant,
    val fileSize: Long,
    val contentType: String,
    val fileName: String,
)
