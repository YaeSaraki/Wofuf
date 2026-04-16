package dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers

import dev.saraki.wofuf.modules.forum.domain.Image
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.ImageEntity
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.MemberEntity
import dev.saraki.wofuf.shared.domain.UniqueEntityId

/**
 * Image领域对象与ImageEntity之间的映射器
 */
object ImageEntityMapper {

    fun toDomain(entity: ImageEntity): Image {
        val uploaderId = entity.uploaderMember?.let {
            MemberId.create(UniqueEntityId(it.memberId)).getOrNull()
        }
        return Image.createWithId(
            id = UniqueEntityId(entity.imageId),
            objectName = entity.objectName,
            md5 = entity.md5,
            folder = entity.folder,
            uploaderId = uploaderId,
            uploadedAt = entity.uploadedAt,
            fileSize = entity.fileSize,
            contentType = entity.contentType,
            fileName = entity.fileName
        )
    }

    fun toEntity(image: Image, uploaderMember: MemberEntity?): ImageEntity {
        return ImageEntity(
            imageId = image.imageId,
            objectName = image.objectName,
            md5 = image.md5,
            folder = image.folder,
            uploaderMember = uploaderMember,
            uploadedAt = image.uploadedAt,
            fileSize = image.fileSize,
            contentType = image.contentType,
            fileName = image.fileName
        )
    }
}
