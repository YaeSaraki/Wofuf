package dev.saraki.wofuf.modules.forum.useCases.admin.posts.featurePost

class FeaturePostDto {
    data class Request(val postId: String)
    data class Response(
        val postId: String,
        val isFeatured: Boolean,
        val message: String = "Post featured successfully"
    )
}
