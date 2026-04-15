package dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers

import dev.saraki.wofuf.modules.forum.domain.Image
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.ImageEntity
import dev.saraki.wofuf.shared.domain.UniqueEntityId

/**
 * Image领域对象与ImageEntity之间的映射器
 */
object ImageEntityMapper {

    fun toDomain(entity: ImageEntity): Image {
        return Image.createWithId(
            id = UniqueEntityId(entity.imageId),
            url = entity.url,
            md5 = entity.md5,
            folder = entity.folder,
            uploaderId = entity.uploaderId,
            uploadedAt = entity.uploadedAt,
            fileSize = entity.fileSize,
            contentType = entity.contentType,
            fileName = entity.fileName
        )
    }

    fun toEntity(image: Image): ImageEntity {
        return ImageEntity(
            imageId = image.imageId,
            url = image.url,
            md5 = image.md5,
            folder = image.folder,
            uploaderId = image.uploaderId,
            uploadedAt = image.uploadedAt,
            fileSize = image.fileSize,
            contentType = image.contentType,
            fileName = image.fileName
        )
    }
}
