package dev.saraki.wofuf.modules.forum.useCases.admin.images.getImages

class GetImagesDto {
    data class Request(
        val page: Int = 0,
        val size: Int = 20,
        val folder: String? = null,
        val uploaderId: String? = null
    )

    data class Response(
        val images: List<ImageSummary>,
        val total: Long,
        val page: Int,
        val size: Int
    )

    data class ImageSummary(
        val imageId: String,
        val url: String,
        val md5: String,
        val folder: String,
        val uploaderId: String?,
        val uploadedAt: Long,
        val fileSize: Long,
        val contentType: String,
        val fileName: String
    )
}
