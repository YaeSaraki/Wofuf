package dev.saraki.wofuf.modules.forum.useCases.posts.unvotePost

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/17
 *   @description: DTOs for unvote post use case
 */
class UnvotePostDto {
    data class Request(
        val postId: String,
        val userId: String
    )

    data class Response(
        val newPoints: Int
    )
}
