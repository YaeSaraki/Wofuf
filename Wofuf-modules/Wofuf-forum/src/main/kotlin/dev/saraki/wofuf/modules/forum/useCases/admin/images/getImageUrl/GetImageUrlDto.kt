package dev.saraki.wofuf.modules.forum.useCases.admin.images.getImageUrl

class GetImageUrlDto {
    data class Request(
        val imageId: String
    )

    data class Response(
        val url: String,
        val expiresInSeconds: Long = 300
    )
}