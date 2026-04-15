package dev.saraki.wofuf.modules.forum.useCases.admin.stats

class GetAdminStatsDto {
    data class Response(
        val totalPosts: Long,
        val totalComments: Long,
        val totalMembers: Long,
        val pendingReview: Long,
        val hiddenPosts: Long,
        val hiddenComments: Long,
        val bannedMembers: Long
    )
}
