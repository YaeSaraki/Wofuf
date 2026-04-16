package dev.saraki.wofuf.modules.forum.useCases.admin.images.getImages

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.ImageRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.MemberJpaRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class GetImagesUseCase(
    private val imageRepo: ImageRepo,
    private val memberJpaRepo: MemberJpaRepo
) : UseCase<GetImagesDto.Request, GetImagesDto.Response> {

    @RequirePermission(PermissionPoint.ADMIN_ACCESS, "Only administrators can view image list")
    override fun execute(request: GetImagesDto.Request): Result<GetImagesDto.Response> {
        val page = request.page.coerceAtLeast(0)
        val size = request.size.coerceIn(1, 100)

        val uploaderMemberId = request.uploaderMemberId?.let {
            MemberId.create(UniqueEntityId(it)).getOrNull()
        }

        val images = imageRepo.findImages(page, size, request.folder, uploaderMemberId)
        val total = imageRepo.countImages(request.folder, uploaderMemberId)

        val imageSummaries = images.map { image ->
            val uploaderNickname = image.uploaderId?.stringValue?.let { memberId ->
                memberJpaRepo.findById(memberId).orElse(null)?.nickname
            }
            GetImagesDto.ImageSummary(
                imageId = image.imageId,
                objectName = image.objectName,
                md5 = image.md5,
                folder = image.folder,
                uploaderMemberId = image.uploaderId?.stringValue,
                uploaderNickname = uploaderNickname,
                uploadedAt = image.uploadedAt.toEpochMilli(),
                fileSize = image.fileSize,
                contentType = image.contentType,
                fileName = image.fileName
            )
        }

        return Result.success(GetImagesDto.Response(
            images = imageSummaries,
            total = total,
            page = page,
            size = size
        ))
    }
}
