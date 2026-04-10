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

    fun findByPostEntity_SlugAndIsHiddenFalse(
        @Param("slug") slug: String
    ): List<CommentEntity>

    fun findByPostEntity_Slug(
        @Param("slug") slug: String
    ): List<CommentEntity>

    fun findRepliesByCommentIdAndIsHiddenFalse(
        @Param("commentId") commentId: String
    ): List<CommentEntity>

    fun findRepliesByCommentId(
        @Param("commentId") commentId: String
    ): List<CommentEntity>

    fun findByPostIdAndIsHiddenFalse(postId: String): List<CommentEntity>

    fun findByPostId(postId: String): List<CommentEntity>

    fun findByIsHiddenTrue(pageable: Pageable): List<CommentEntity>

    fun countByIsHiddenTrue(): Long

    // 分页获取所有评论，按创建时间倒序
    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): List<CommentEntity>
}