package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentText
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostCategory
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostLink
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostText
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostTitle
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostType
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.AggregateRoot
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 14:03
 *   @description:
 */
data class PostProps(
    val memberId: MemberId,
    val slug: PostSlug,
    val title: PostTitle,
    val type: PostType,
    val category: PostCategory = PostCategory.DISCUSSION,
    val text: PostText?,
    val link: PostLink?,
    val comments: Comments,
    val votes: PostVotes,
    val totalNumComments: Int?,
    var points: Int,
    val dateTimePosted: LocalDateTime
)

class Post private constructor(
    props: PostProps,
    id: UniqueEntityId?
) : AggregateRoot<PostProps>(props, id) {
    val postId: PostId
        get() = PostId.create(_id).getOrThrow()

    val memberId: MemberId
        get() = props.memberId

    val slug: PostSlug
        get() = props.slug

    val title: PostTitle
        get() = props.title

    val type: PostType
        get() = props.type

    val category: PostCategory
        get() = props.category

    val text: PostText?
        get() = props.text

    val link: PostLink?
        get() = props.link

    val comments: Comments
        get() = props.comments

    val votes: PostVotes
        get() = props.votes

    val totalNumComments: Int?
        get() = props.totalNumComments

    val points: Int
        get() {
            val initialValue = props.points
            return initialValue + computeVotePoints()
        }

    val dateTimePosted: LocalDateTime
        get() = props.dateTimePosted

    /**
     * 新添加的投票：UPVOTE+1 / DOWNVOTE-1
     * 被移除的投票：UPVOTE-1 / DOWNVOTE+1
     */
    private fun computeVotePoints(): Int {
        var tally = 0
        // 遍历新添加的投票，计算积分
        props.votes.getNewItems().forEach { vote ->
            if (vote.isUpVote()) tally++
            if (vote.isDownVote()) tally--
        }
        // 遍历被移除的投票，回滚积分
        props.votes.getRemovedItems().forEach { vote ->
            if (vote.isUpVote()) tally--
            if (vote.isDownVote()) tally++
        }
        return tally
    }

    fun addVote(vote: PostVote): Result<Unit> {
        props.votes.add(vote)
        return Result.success(Unit)
    }

    fun removeVote(vote: PostVote): Result<Unit> {
        props.votes.remove(vote)
        return Result.success(Unit)
    }

    /**
     * 更新基础积分（总点赞-总点踩）
     * 用于从持久化层加载最新基础积分，而非计算内存中的变更
     */
    fun updateScore(totalNumUpvotes: Int, totalNumDownvotes: Int) {
        props.points = totalNumUpvotes - totalNumDownvotes
    }

    fun hasUpvotedBy(memberId: MemberId): Boolean {
        return props.votes.getItems().any { it.memberId == memberId && it.isUpVote() }
    }

    fun hasDownvotedBy(memberId: MemberId): Boolean {
        return props.votes.getItems().any { it.memberId == memberId && it.isDownVote() }
    }

    fun getVoteByMember(memberId: MemberId): PostVote? {
        return props.votes.getItems().find { it.memberId == memberId }
    }

    fun addComment(memberId: MemberId, postId: PostId, text: CommentText, parentCommentId: CommentId?): Result<Post> {
        val commentProps = CommentProps(
            postId = postId,
            text = text,
            memberId = memberId,
            parentCommentId = parentCommentId,
            points = 0,
            votes = CommentVotes.create(),
        )
        val comment = Comment.create(commentProps).getOrThrow()
        this.comments.add(comment)
        return Result.success(this)
    }

    fun edit(
        title: PostTitle? = null,
        text: PostText? = null,
        link: PostLink? = null,
        category: PostCategory? = null
    ): Result<Post> {
        val newProps = PostProps(
            memberId = props.memberId,
            slug = props.slug,
            title = title ?: props.title,
            type = props.type,
            category = category ?: props.category,
            text = text ?: props.text,
            link = link ?: props.link,
            comments = props.comments,
            votes = props.votes,
            totalNumComments = props.totalNumComments,
            points = props.points,
            dateTimePosted = props.dateTimePosted
        )
        return Post.create(newProps, _id)
    }

    companion object {
        fun create(props: PostProps, id: UniqueEntityId? = null): Result<Post> {
            Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(props.memberId, "memberId"),
                    Guard.GuardArgument(props.slug, "slug"),
                    Guard.GuardArgument(props.title, "title"),
                    Guard.GuardArgument(props.type, "type"),
                )
            )
            return Result.success(Post(props, id))
        }


    }
}
