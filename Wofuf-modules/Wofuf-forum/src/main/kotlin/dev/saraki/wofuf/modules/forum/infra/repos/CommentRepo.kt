package dev.saraki.wofuf.modules.forum.infra.repos

import dev.saraki.wofuf.modules.forum.domain.*
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
}