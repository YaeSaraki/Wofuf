package dev.saraki.wofuf.modules.forum.domain.services

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteResult
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteStatus
import dev.saraki.wofuf.shared.core.Result

/**
 * 帖子投票领域服务 - 处理跨聚合的投票操作
 */
interface PostVoteDomainService {
    /**
     * 获取单个帖子的投票状态
     */
    fun getVoteStatus(postId: PostId, memberId: MemberId): VoteStatus

    /**
     * 批量获取多个帖子的投票状态（避免 N+1 查询）
     */
    fun getVoteStatuses(postIds: List<PostId>, memberId: MemberId): Map<PostId, VoteStatus>

    /**
     * 点赞（支持 Toggle：已点赞则取消）
     */
    fun upvote(postId: PostId, memberId: MemberId): Result<VoteResult>

    /**
     * 点踩（支持 Toggle：已点踩则取消）
     */
    fun downvote(postId: PostId, memberId: MemberId): Result<VoteResult>
}
