package dev.saraki.wofuf.modules.forum.infra.repos

import dev.saraki.wofuf.modules.forum.domain.CommentVote
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteType

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/7 16:17
 *   @description:
 */
interface CommentVotesRepo {
    fun exists(commentId: CommentId, memberId: MemberId, voteType: VoteType): Boolean
    fun findByCommentIdAndMemberId(commentId: CommentId, memberId: MemberId): CommentVote?
    fun save(vote: CommentVote): CommentVote
    fun delete(vote: CommentVote)
    fun countCommentUpvotesByCommentId(commentId: CommentId): Int
    fun countCommentDownvotesByCommentId(commentId: CommentId): Int
    fun flush()
    
    /**
     * 批量查询多个评论的投票状态（避免 N+1 查询）
     */
    fun findByCommentIdsAndMemberId(commentIds: List<String>, memberId: String): List<CommentVote>
}