package dev.saraki.wofuf.modules.forum.infra.repos.jpa

import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.CommentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 14:11
 *   @description:
 */
interface CommentJpaRepo : JpaRepository<CommentEntity, String> {

    fun findByPostEntity_Slug(
        @Param("slug") slug: String
    ): List<CommentEntity>

    fun findRepliesByCommentId(
        @Param("commentId") commentId: String
    ): List<CommentEntity>

    fun findByPostId(postId: String): List<CommentEntity>
}