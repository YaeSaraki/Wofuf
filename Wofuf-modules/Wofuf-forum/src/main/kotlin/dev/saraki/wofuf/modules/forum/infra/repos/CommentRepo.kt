package dev.saraki.wofuf.modules.forum.infra.repos

import dev.saraki.wofuf.modules.forum.domain.Comment
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentDetails
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 13:55
 *   @description:
 */

interface CommentRepo {
    fun exists(commentId: CommentId): Boolean
    fun findCommentByCommentId(commentId: CommentId): Comment?
    fun findCommentsByPostSlug(postSlug: PostSlug): List<Comment>
    fun findCommentDetailsByCommentId(commentId: CommentId): CommentDetails?
    fun save(comment: Comment): Comment
    fun saveBulk(comments: List<Comment>)
    fun deleteComment(commentId: CommentId)

    // ==================== 管理功能方法 ====================

    /**
     * 分页获取隐藏评论列表
     */
    fun findHiddenComments(page: Int, size: Int): List<Comment>

    /**
     * 统计隐藏评论数量
     */
    fun countHiddenComments(): Long

    /**
     * 统计评论总数
     */
    fun countAll(): Long
}