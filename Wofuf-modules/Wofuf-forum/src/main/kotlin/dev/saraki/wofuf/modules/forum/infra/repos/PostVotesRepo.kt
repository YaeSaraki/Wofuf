package dev.saraki.wofuf.modules.forum.infra.repos

import dev.saraki.wofuf.modules.forum.domain.*
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostVote
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
    fun saveBulk(postVotes: PostVotes)
    fun save(postVote: PostVote): PostVote
    fun delete(postVote: PostVote)
}