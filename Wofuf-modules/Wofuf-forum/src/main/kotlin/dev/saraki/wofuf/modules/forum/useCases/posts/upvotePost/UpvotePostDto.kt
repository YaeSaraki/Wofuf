package dev.saraki.wofuf.modules.forum.useCases.posts.upvotePost

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Data transfer objects for upvoting a post
 */
class UpvotePostDto {
    data class Request(
        val postId: String,
        val userId: String,
    )

    data class Response(
        val success: Boolean = true,
        val newPoints: Int,
    )
}
