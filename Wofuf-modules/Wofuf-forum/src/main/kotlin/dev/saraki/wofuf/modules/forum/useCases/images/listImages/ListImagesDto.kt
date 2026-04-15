package dev.saraki.wofuf.modules.forum.useCases.images.listImages

class ListImagesDto {
    data class Request(
        val folder: String? = null,
        val uploaderId: String? = null
    )

    data class Response(
        val images: List<ImageInfo>
    )

    data class ImageInfo(
        val imageId: String,
        val md5: String,
        val url: String,
        val folder: String,
        val fileName: String,
        val fileSize: Long,
        val contentType: String,
        val uploadedAt: Long
    )
}
