package dev.saraki.wofuf.modules.forum.useCases.posts.createPost

class CreatePostDto {
    data class Request(
        val title: String,
        val type: String,
        val text: String? = null,
        val link: String? = null,
    )

    data class Response(
        val postId: String,
        val slug: String,
        val success: Boolean = true,
    )
}
