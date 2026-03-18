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
    fun findByCommentIdInAndMemberId(commentIds: List<String>, memberId: String): List<CommentVoteEntity>

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(v) FROM CommentVoteEntity v WHERE v.commentId = :commentId AND v.voteType = 'UPVOTE'")
    fun countUpvotesByCommentId(commentId: String): Int

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(v) FROM CommentVoteEntity v WHERE v.commentId = :commentId AND v.voteType = 'DOWNVOTE'")
    fun countDownvotesByCommentId(commentId: String): Int
}