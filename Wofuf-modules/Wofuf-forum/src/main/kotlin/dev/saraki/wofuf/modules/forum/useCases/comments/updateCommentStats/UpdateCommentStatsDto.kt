package dev.saraki.wofuf.modules.forum.useCases.comments.updateCommentStats

class UpdateCommentStatsDto {
    data class Request(
        val commentId: String,
    )

    data class Response(
        val success: Boolean = true,
        val updatedPoints: Int,
    )
}
