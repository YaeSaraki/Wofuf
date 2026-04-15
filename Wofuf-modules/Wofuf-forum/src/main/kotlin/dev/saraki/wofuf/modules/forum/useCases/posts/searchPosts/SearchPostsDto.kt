package dev.saraki.wofuf.modules.forum.useCases.posts.searchPosts

import dev.saraki.wofuf.modules.forum.dtos.PostDto

/**
 * Data transfer objects for searching posts
 */
class SearchPostsDto {
    data class Request(
        val query: String,       // 搜索关键词
        val page: Int = 0,       // 页码，从 0 开始
        val size: Int = 10,      // 每页数量
        val category: String? = null,  // 分类筛选
    )

    data class Response(
        val posts: List<PostDto>
    )
}
