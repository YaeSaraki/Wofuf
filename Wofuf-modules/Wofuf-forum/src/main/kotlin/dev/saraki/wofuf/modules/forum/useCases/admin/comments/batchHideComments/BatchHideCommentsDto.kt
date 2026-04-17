package dev.saraki.wofuf.modules.forum.useCases.admin.comments.batchHideComments

class BatchHideCommentsDto {
    data class Request(
        val commentIds: List<String>,
        val userId: String,  // 当前操作用户的 ID
    )

    data class Response(
        val successCount: Int,
        val failCount: Int,
        val results: List<BatchResult>,
        val message: String = "Operation completed"
    )

    data class BatchResult(
        val commentId: String,
        val isHidden: Boolean,
        val success: Boolean,
        val message: String
    )
}
