package dev.saraki.wofuf.modules.forum.useCases.posts.getPostBySlug

import dev.saraki.wofuf.modules.forum.dtos.PostDto

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Data transfer objects for getting a post by slug
 */
class GetPostBySlugDto {
    data class Request(
        val postSlug: String,
        val userId: String? = null,
    )

    data class Response(
        val post: PostDto
    )
}
