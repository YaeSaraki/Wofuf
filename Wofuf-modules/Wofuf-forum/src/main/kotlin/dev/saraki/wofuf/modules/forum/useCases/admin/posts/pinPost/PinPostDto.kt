package dev.saraki.wofuf.modules.forum.useCases.admin.posts.pinPost

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/4/8
 * @description Data transfer objects for pinning a post
 */
class PinPostDto {
    data class Request(
        val postId: String,
        val operatorMemberId: String,
    )

    data class Response(
        val postId: String,
        val isPinned: Boolean,
        val message: String = "Post pinned successfully"
    )
}
