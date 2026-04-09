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
        val currentUserId: String,  // 当前登录用户的 ID（从 SecurityContextHolder 获取）
        val title: String? = null,
        val text: String? = null,
        val link: String? = null,
    )

    data class Response(
        val postId: String,
        val title: String,
        val text: String?,
        val link: String?,
        val success: Boolean = true,
    )
}
