package dev.saraki.wofuf.modules.forum.infra.repos

import dev.saraki.wofuf.modules.forum.domain.Comment
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentDetails
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
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
    fun findCommentsByPostSlug(postSlug: PostSlug, includeHidden: Boolean = false): List<Comment>

    /**
     * 获取帖子的所有主评论（parentCommentId IS NULL）
     * 用于 Bilibili 风格评论
     */
    fun findRootCommentsByPostSlug(postSlug: PostSlug, includeHidden: Boolean = false): List<Comment>
    fun findCommentDetailsByCommentId(commentId: CommentId): CommentDetails?

    /**
     * 批量查询评论详情（消除 N+1 查询）
     */
    fun findCommentDetailsByCommentIds(commentIds: List<CommentId>): Map<CommentId, CommentDetails>

    /**
     * 按主评论ID查询所有子评论（用于 Bilibili 风格评论）
     * @param rootCommentId 主评论ID
     * @param includeHidden 是否包含隐藏评论
     */
    fun findChildCommentsByRootId(rootCommentId: CommentId, includeHidden: Boolean = false): List<Comment>

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

    /**
     * 分页获取所有评论（不区分隐藏状态）
     */
    fun findAllComments(page: Int, size: Int): List<Comment>

    /**
     * 统计所有评论数量（不区分隐藏状态）
     */
    fun countAllComments(): Long

    /**
     * 按内容关键词搜索评论（模糊匹配）
     */
    fun findCommentsByContentSearch(contentSearch: String, page: Int, size: Int, includeHidden: Boolean): List<Comment>

    /**
     * 统计按内容关键词搜索的评论数量
     */
    fun countCommentsByContentSearch(contentSearch: String, includeHidden: Boolean): Long

    // ==================== 成员资料页方法 ====================

    /**
     * 按成员ID分页查询评论
     */
    fun findCommentsByMemberId(memberId: MemberId, page: Int, size: Int): List<Comment>

    /**
     * 统计成员评论数量
     */
    fun countCommentsByMemberId(memberId: MemberId): Long

    /**
     * 删除评论（仅当评论属于指定成员时）
     */
    fun deleteByIdAndMemberId(commentId: CommentId, memberId: MemberId): Boolean
}