package dev.saraki.wofuf.modules.forum.domain.services

import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteResult
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteStatus
import dev.saraki.wofuf.shared.core.Result

/**
 * 评论投票领域服务 - 处理跨聚合的投票操作
 */
interface CommentVoteDomainService {
    /**
     * 获取单个评论的投票状态
     */
    fun getVoteStatus(commentId: CommentId, memberId: MemberId): VoteStatus

    /**
     * 批量获取多个评论的投票状态（避免 N+1 查询）
     */
    fun getVoteStatuses(commentIds: List<CommentId>, memberId: MemberId): Map<CommentId, VoteStatus>

    /**
     * 点赞（支持 Toggle：已点赞则取消）
     */
    fun upvote(commentId: CommentId, memberId: MemberId): Result<VoteResult>

    /**
     * 点踩（支持 Toggle：已点踩则取消）
     */
    fun downvote(commentId: CommentId, memberId: MemberId): Result<VoteResult>
}
