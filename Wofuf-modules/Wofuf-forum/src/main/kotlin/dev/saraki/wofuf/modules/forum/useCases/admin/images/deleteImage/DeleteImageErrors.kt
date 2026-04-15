package dev.saraki.wofuf.modules.forum.useCases.admin.images.deleteImage

sealed class DeleteImageErrors {
    class ImageNotFoundError(imageId: String) : Exception("Image not found: $imageId")
    class DeleteFailedError(message: String) : Exception(message)
}
