package dev.saraki.wofuf.modules.forum.infra.repos.jpa

import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.CommentVoteEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 15:30
 *   @description: CommentVotes实体的JPA仓库接口
 */
interface CommentVotesJpaRepo : JpaRepository<CommentVoteEntity, String> {
    fun existsByCommentIdAndMemberIdAndVoteType(commentId: String, memberId: String, voteType: String): Boolean
    fun findByCommentIdAndMemberId(commentId: String, memberId: String): CommentVoteEntity?
}