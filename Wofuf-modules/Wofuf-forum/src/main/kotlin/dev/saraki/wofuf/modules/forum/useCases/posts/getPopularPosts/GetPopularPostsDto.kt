package dev.saraki.wofuf.modules.forum.useCases.posts.getPopularPosts

import dev.saraki.wofuf.modules.forum.dtos.PostDto

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Data transfer objects for getting popular posts
 */
class GetPopularPostsDto {
    data class Request(
        val offset: Int? = 10,
        val userId: String? = null,
    )

    data class Response(
        val posts: List<PostDto>
    )
}
