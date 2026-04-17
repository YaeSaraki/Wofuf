package dev.saraki.wofuf.modules.forum.useCases.posts.editPost

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Data transfer objects for editing a post
 */
class EditPostDto {
    data class Request(
        val postId: String,
        val currentUserId: String? = null,  // 后端从 SecurityContextHolder 获取，前端不需要传
        val title: String? = null,
        val text: String? = null,
        val link: String? = null,
        val category: String? = null,
    )

    data class Response(
        val postId: String,
        val title: String,
        val text: String?,
        val link: String?,
        val category: String,
        val success: Boolean = true,
    )
}
