package dev.saraki.wofuf.modules.forum.useCases.posts.deletePost

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Data transfer objects for deleting a post
 */
class DeletePostDto {
    data class Request(
        val postId: String,
        val currentUserId: String,  // 当前登录用户的 ID（从 SecurityContextHolder 获取）
    )

    data class Response(
        val success: Boolean = true,
        val message: String = "Post deleted successfully"
    )
}
