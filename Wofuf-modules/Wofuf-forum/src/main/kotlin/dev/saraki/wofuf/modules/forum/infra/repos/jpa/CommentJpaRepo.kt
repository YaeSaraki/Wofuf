package dev.saraki.wofuf.modules.forum.infra.repos.jpa

import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.CommentEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 14:11
 *   @description:
 */
interface CommentJpaRepo : JpaRepository<CommentEntity, String> {

    // 批量查询评论（用于消除 N+1 查询）
    fun findByCommentIdIn(ids: List<String>): List<CommentEntity>

    fun findByPostEntity_SlugAndIsHiddenFalse(
        @Param("slug") slug: String
    ): List<CommentEntity>

    fun findByPostEntity_Slug(
        @Param("slug") slug: String
    ): List<CommentEntity>

    // 查询主评论（parentCommentId IS NULL）- 用于 Bilibili 风格评论
    fun findByPostEntity_SlugAndParentCommentIdIsNullAndIsHiddenFalse(
        @Param("slug") slug: String
    ): List<CommentEntity>

    fun findByPostEntity_SlugAndParentCommentIdIsNull(
        @Param("slug") slug: String
    ): List<CommentEntity>

    fun findRepliesByCommentIdAndIsHiddenFalse(
        @Param("commentId") commentId: String
    ): List<CommentEntity>

    fun findRepliesByCommentId(
        @Param("commentId") commentId: String
    ): List<CommentEntity>

    // 按 rootCommentId 查询所有子评论（用于 Bilibili 风格评论）
    fun findByRootCommentIdAndIsHiddenFalse(
        @Param("rootCommentId") rootCommentId: String
    ): List<CommentEntity>

    fun findByRootCommentId(
        @Param("rootCommentId") rootCommentId: String
    ): List<CommentEntity>

    fun findByPostIdAndIsHiddenFalse(postId: String): List<CommentEntity>

    fun findByPostId(postId: String): List<CommentEntity>

    fun findByIsHiddenTrue(pageable: Pageable): List<CommentEntity>

    fun countByIsHiddenTrue(): Long

    // 分页获取所有评论，按创建时间倒序
    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): List<CommentEntity>

    // 按内容关键词搜索（模糊匹配）
    fun findByTextContainingIgnoreCase(text: String, pageable: Pageable): List<CommentEntity>

    // 按内容关键词搜索并包含隐藏状态
    fun findByTextContainingIgnoreCaseAndIsHidden(text: String, isHidden: Boolean, pageable: Pageable): List<CommentEntity>

    // 统计内容关键词搜索结果数量
    fun countByTextContainingIgnoreCase(text: String): Long

    // 统计内容关键词搜索结果数量（按隐藏状态）
    fun countByTextContainingIgnoreCaseAndIsHidden(text: String, isHidden: Boolean): Long
}