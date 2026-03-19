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
        val page: Int = 0,      // 页码，从 0 开始
        val size: Int = 10,     // 每页数量
        val userId: String? = null,
        val category: String? = null,  // 分类筛选参数
    )

    data class Response(
        val posts: List<PostDto>
    )
}
