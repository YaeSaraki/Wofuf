package dev.saraki.wofuf.modules.forum.useCases.posts.getPostById

import dev.saraki.wofuf.modules.forum.dtos.PostDto

/**
 * @author YaeSaraki
 * @date 2026/4/16
 * @description Data transfer objects for getting a post by ID
 */
class GetPostByIdDto {
    data class Request(
        val postId: String,
        val userId: String? = null,
    )

    data class Response(
        val post: PostDto
    )
}