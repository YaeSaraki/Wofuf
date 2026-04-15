package dev.saraki.wofuf.modules.forum.useCases.admin.members.getMemberProfile

import java.time.LocalDateTime

class GetMemberProfileDto {
    data class Request(
        val memberId: String,
        val page: Int = 0,
        val size: Int = 10
    )

    data class Response(
        val memberId: String,
        val userId: String,
        val playerId: String,
        val nickname: String,
        val reputation: Int,
        val permissions: List<String>,
        val isBanned: Boolean,
        val bannedAt: String?,
        val bannedUntil: String?,
        val bannedReason: String?,
        val bannedBy: String?,
        val postHistory: List<PostSummary>,
        val totalPosts: Long
    )

    data class PostSummary(
        val postId: String,
        val slug: String,
        val title: String,
        val category: String,
        val points: Int,
        val numComments: Int,
        val status: String,
        val isPinned: Boolean,
        val isFeatured: Boolean,
        val createdAt: LocalDateTime
    )
}
