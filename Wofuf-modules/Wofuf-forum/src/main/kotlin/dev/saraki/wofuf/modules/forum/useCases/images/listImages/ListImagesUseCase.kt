package dev.saraki.wofuf.modules.forum.useCases.images.listImages

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.infra.repos.ImageRepo
import dev.saraki.wofuf.modules.forum.infra.storage.ImageStorageService
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class ListImagesUseCase(
    private val imageRepo: ImageRepo,
    private val imageStorageService: ImageStorageService
) : UseCase<ListImagesDto.Request, ListImagesDto.Response> {

    override fun execute(request: ListImagesDto.Request): Result<ListImagesDto.Response> {
        val page = 0
        val size = 100 // 获取足够多的图片

        val uploaderMemberId = request.uploaderMemberId?.let {
            MemberId.create(UniqueEntityId(it)).getOrNull()
        }

        val images = imageRepo.findImages(page, size, request.folder, uploaderMemberId)

        val imageInfos = images
            .filter { it.objectName.isNotBlank() } // 只返回有有效objectName的图片
            .map { image ->
                ListImagesDto.ImageInfo(
                    imageId = image.imageId,
                    md5 = image.md5,
                    url = "/api/v1/forum/images/" + image.md5,
                    folder = image.folder,
                    fileName = image.fileName,
                    fileSize = image.fileSize,
                    contentType = image.contentType,
                    uploadedAt = image.uploadedAt.toEpochMilli()
                )
            }

        return Result.success(ListImagesDto.Response(images = imageInfos))
    }
}
