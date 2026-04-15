package dev.saraki.wofuf.modules.forum.useCases.admin.images.deleteImage

class DeleteImageDto {
    data class Request(
        val imageId: String
    )

    data class Response(
        val success: Boolean,
        val message: String
    )
}
