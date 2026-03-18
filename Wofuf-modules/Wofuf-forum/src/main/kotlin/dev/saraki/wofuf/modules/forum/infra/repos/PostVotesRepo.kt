package dev.saraki.wofuf.modules.forum.infra.repos

import dev.saraki.wofuf.modules.forum.domain.*
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.domain.PostVote
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteType

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 13:38
 *   @description:
 */
interface PostVotesRepo {
    fun exists(postId: PostId, memberId: MemberId, voteType: VoteType): Boolean
    fun findByPostIdAndMemberId(postId: PostId, memberId: MemberId): List<PostVote>
    fun countPostUpvotesByPostId(postId: PostId): Int
    fun countPostDownvotesByPostId(postId: PostId): Int
    fun save(postVote: PostVote): PostVote
    fun delete(postVote: PostVote)
    fun deleteByPostIdAndMemberId(postId: PostId, memberId: MemberId)
    fun flush()
    
    /**
     * 批量查询多个帖子的投票状态（避免 N+1 查询）
     */
    fun findByPostIdsAndMemberId(postIds: List<String>, memberId: String): List<PostVote>
}